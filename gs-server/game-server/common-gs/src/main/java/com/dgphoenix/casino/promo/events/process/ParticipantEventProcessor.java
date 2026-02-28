package com.abs.casino.promo.events.process;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.DistributedLockManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.lock.LockingInfo;
import com.dgphoenix.casino.common.promo.*;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItem;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItemType;
import com.abs.casino.common.transactiondata.storeddate.identifier.StoredItemInfo;
import com.dgphoenix.casino.common.util.IdGenerator;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.gs.managers.dblink.IGameDBLink;
import com.abs.casino.promo.PromoCampaignManager;
import com.abs.casino.promo.TournamentRankCalculatorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: flsh
 * Date: 17.02.17.
 */
@SuppressWarnings("rawtypes")
public class ParticipantEventProcessor {
    private static final Logger LOG = LogManager.getLogger(ParticipantEventProcessor.class);
    private final IPromoCampaignManager campaignManager;
    private final Map<SignificantEventType, ParticipantEventQualifier> eventQualifiersHolder;
    private final IRemotePromoNotifier remotePromoNotifier;
    private final ICurrencyRateManager currencyRateManager;
    private final DistributedLockManager lockManager;
    private final TournamentRankCalculatorService tournamentRankCalculator;

    public ParticipantEventProcessor(CassandraPersistenceManager persistenceManager, IPromoCampaignManager campaignManager,
                                     Map<SignificantEventType, ParticipantEventQualifier> eventQualifiersHolder,
                                     ICurrencyRateManager currencyRateManager, IRemotePromoNotifier remotePromoNotifier,
                                     TournamentRankCalculatorService tournamentRankCalculatorService) {
        this.campaignManager = campaignManager;
        this.eventQualifiersHolder = eventQualifiersHolder;
        this.remotePromoNotifier = remotePromoNotifier;
        this.currencyRateManager = currencyRateManager;
        lockManager = persistenceManager.getPersister(DistributedLockManager.class);
        tournamentRankCalculator = tournamentRankCalculatorService;
    }

    public <T extends IParticipantEvent> void process(T event, IPromoCampaign campaign, IGameDBLink dbLink) throws CommonException {
        long now = System.currentTimeMillis();
        @SuppressWarnings("unchecked")
        ParticipantEventQualifier<IParticipantEvent> eventQualifier = (ParticipantEventQualifier<IParticipantEvent>)
                eventQualifiersHolder.get(event.getType());
        if (eventQualifier == null) {
            LOG.warn("Skip processing, found unsupported event type: {}", event.getType());
            return;
        }
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        PromoCampaignMember member = transactionData.getPromoMember(campaign.getId());
        if (member == null) {
            LOG.error("process: PromoMember not found for campaignId: {}", campaign.getId());
            return;
        }
        AccountInfo account = transactionData.getAccount();
        if (!campaignManager.isPlayerIncludedInPromo(campaign, account)) {
            LOG.warn("commonProcess: may be error, player cannot enter to promo.id={}, account.id={}", campaign.getId(), account.getId());
            //but, this be normal, if player removed from promo after enter
            return;
        }
        EventProcessorContext context = new EventProcessorContext(campaign, member, account);
        List<DesiredPrize> activeDesiredPrizes = member.getActiveDesiredPrizes();
        for (int i = 0; i < activeDesiredPrizes.size(); i++) {
            DesiredPrize desiredPrize = activeDesiredPrizes.get(i);
            IPrize prize = campaign.getPrize(desiredPrize.getPromoPrizeId());
            if (prize == null) {
                LOG.error("Very strange, cannot find prize with id={}, desiredPrize={}, campaign={}, campaignMember={}",
                        desiredPrize.getPromoPrizeId(), desiredPrize, campaign, member);
                continue;
            }
            IPromoTemplate<?,?> template = campaign.getTemplate();
            boolean active = template.checkIfDesiredPrizeActive(desiredPrize, campaign);
            if (!active) {
                DesiredPrize newDesiredPrize = template.createNewDesiredPrize(desiredPrize, campaign, prize);
                if (newDesiredPrize != null) {
                    LOG.debug("process: new desired prize created: {}", newDesiredPrize);
                    context.replaceDesiredPrize(desiredPrize, newDesiredPrize);
                    desiredPrize = newDesiredPrize;
                } else {
                    continue;
                }
            }
            boolean eventQualified = eventQualifier.qualifyEvent(event, prize, desiredPrize, member, campaign, i == 0);
            if (eventQualified) {
                LOG.debug("process: Event qualified: event={}, desiredPrize={}, member={}", event, desiredPrize, member);
                updateMemberRank(context, event, prize, desiredPrize, transactionData);
                boolean prizeQualified;
                AbstractParticipantEvent abstractParticipantEvent = ((AbstractParticipantEvent) event);
                do {
                    PromoType promoType = template.getPromoType();
                    if (abstractParticipantEvent.hasWonPromoType(promoType) && promoType.isSingleWinPerMultiplePromosAtOnce()) {
                        break;
                    }
                    prizeQualified = tryQualifyPrize(dbLink, context, desiredPrize, prize, abstractParticipantEvent, promoType);
                } while (prizeQualified);
            }
        }
        context.updateMemberPrizeList();
        updatePromoMember(transactionData, member);
        sendNotification(campaign.getId(), transactionData.getPlayerSession(), member, context.getNotifications());
        StatisticsManager.getInstance().updateRequestStatistics("ParticipantEventProcessor: process",
                System.currentTimeMillis() - now);
    }

    private <T extends IParticipantEvent> void updateMemberRank(EventProcessorContext context, T event, IPrize prize, DesiredPrize desiredPrize,
                                                                ITransactionData transactionData) throws CommonException {
        if (context.getCampaign().getTemplate() instanceof ITournamentPromoTemplate) {
            tournamentRankCalculator.calculate(event, context.getMember(), prize, desiredPrize, context.getCampaign(), context.getAccount(),
                    transactionData);
        }
    }

    private boolean tryQualifyPrize(IGameDBLink dbLink, EventProcessorContext context, DesiredPrize desiredPrize, IPrize prize,
                                    AbstractParticipantEvent event, PromoType promoType) throws CommonException {
        boolean prizeQualified;
        Pair<Boolean, LockingInfo> qualifyResult = qualifyPrize(context, desiredPrize, prize);
        try {
            prizeQualified = qualifyResult != null && qualifyResult.getKey() != null && qualifyResult.getKey();
            //campaign and prize may be changed, need reload
            IPromoCampaign actualCampaign = campaignManager.getPromoCampaign(context.getCampaign().getId());
            IPrize actualPrize = actualCampaign.getPrize(prize.getId());
            if (prizeQualified) {
                processWonPrize(dbLink, context.getMember(), desiredPrize, actualPrize, context.getAccount(), actualCampaign);
                context.addNotification(PromoNotificationType.PRIZE_WON);
                event.addWonPromoType(promoType);
                if (actualPrize instanceof TicketPrize) {
                    IPrizeQualifier prizeQualifier = ((AbstractPrize) actualPrize).getPrizeQualifier();
                    if (!prizeQualifier.isMultiplePrizesAtOnce()) {
                        prizeQualified = false;
                    }
                }
            }
        } finally {
            if (qualifyResult != null && qualifyResult.getValue() != null) {
                lockManager.unlock(qualifyResult.getValue());
            }
        }
        return prizeQualified;
    }

    private void sendNotification(long campaignId, SessionInfo playerSession, PromoCampaignMember member, Set<PromoNotificationType> notifications) {
        if (playerSession != null) {
            String sessionId = playerSession.getSessionId();
            if (!notifications.isEmpty() && member.hasWebSocketSupport()) {
                remotePromoNotifier.sendPromoNotifications(member.getAccountId(), sessionId, campaignId, notifications, member.getLastEnteredServerId());
            }
        }
    }

    private void updatePromoMember(ITransactionData transactionData, PromoCampaignMember promoMember) throws CommonException {
        StoredItem<PromoCampaignMemberInfos, StoredItemInfo<PromoCampaignMemberInfos>> membersItem = transactionData.get(StoredItemType.PROMO_MEMBERS);
        PromoCampaignMemberInfos promoMembers;
        if (membersItem != null) {
            promoMembers = membersItem.getItem();
        } else {
            promoMembers = new PromoCampaignMemberInfos();
            transactionData.add(StoredItemType.PROMO_MEMBERS, promoMembers, null);
        }
        if (!promoMembers.getPromoMembers().containsKey(promoMember.getCampaignId())) {
            promoMembers.add(promoMember);
        }
    }

    private void processWonPrize(IGameDBLink dbLink, PromoCampaignMember member, DesiredPrize desiredPrize, IPrize prize,
                                 AccountInfo account, IPromoCampaign campaign) throws CommonException {
        IPromoTemplate<?,?> template = campaign.getTemplate();
        awardPrize(prize, member, desiredPrize, account.getId());
        template.processWonPrize(desiredPrize);
        IPrizeWonHandler<IPrize> wonHandler = campaignManager.getWonHandlersFactory().getHandler(prize);
        IPrizeWonHelper balanceChanger = new PrizeWonBalanceChanger(dbLink, account, template);
        wonHandler.handle(member, desiredPrize, balanceChanger, prize);
    }

    private Pair<Boolean, LockingInfo> qualifyPrize(EventProcessorContext context, DesiredPrize desiredPrize, IPrize prize) throws CommonException {
        Pair<Boolean, LockingInfo> result;
        IPromoTemplate<?,?> template = context.getCampaign().getTemplate();
        if (template instanceof IConcurrentPromoTemplate) {
            IConcurrentPromoTemplate<?, ?> concurrentPromoTemplate = (IConcurrentPromoTemplate<?, ?>) template;
            result = ((PromoCampaignManager) campaignManager).qualifyConcurrentPrizeWithoutUnlock(context.getCampaign(), concurrentPromoTemplate,
                    prize, context.getMember(), desiredPrize, currencyRateManager, context.getCurrencyCode());
        } else {
            boolean prizeQualified = template.qualifyPrize(prize, context.getMember(), desiredPrize, currencyRateManager,
                    context.getCampaign().getBaseCurrency(), context.getCurrencyCode());
            result = new Pair<>(prizeQualified, null);
        }
        return result;
    }

    private void awardPrize(IPrize prize, PromoCampaignMember member, DesiredPrize desiredPrize, long accountId) {
        long awardId = IdGenerator.getInstance().getNext(AwardedPrize.class);
        long awardTime = System.currentTimeMillis();
        if (prize instanceof IHighFrequencyPrize) {
            awardHFPrize(prize, desiredPrize, awardId, awardTime, member);
        } else {
            AwardedPrize awardedPrize = new AwardedPrize(awardId, prize.getId(), awardTime);
            awardedPrize.updateStatistics(desiredPrize);
            member.addAwardedPrize(awardedPrize);
        }
        LOG.debug("process: Found prize win, accountId={}, prize={}, desiredPrize={}", accountId, prize, desiredPrize);
    }

    private void awardHFPrize(IPrize prize, DesiredPrize desiredPrize, long awardId, long awardTime, PromoCampaignMember member) {
        AwardedPrize awardedPrize = getOrCreateAwardedPrize(prize.getId(), awardId, awardTime, member);
        awardedPrize.updateStatistics(desiredPrize);
        if (prize instanceof TicketPrize) {
            TicketPrize ticketPrize = (TicketPrize) prize;
            IPrizeQualifier prizeQualifier = ticketPrize.getPrizeQualifier();
            if (prizeQualifier instanceof BetAmountPrizeQualifier && prizeQualifier.isMultiplePrizesAtOnce()) {
                awardedPrize.setHighFrequencyAwardedCount(desiredPrize.getReceivedPrizesCount());
                int awardedPrizesAtOnce = desiredPrize.getReceivedPrizesCount() - desiredPrize.getPrevReceivedPrizesCount();
                if (awardedPrizesAtOnce > 0) {
                    awardedPrize.setUsendedNotificationsAwardedCount(awardId, awardedPrizesAtOnce);
                } else {
                    LOG.warn("possible error: awardedPrizesAtOnce={}, awardedPrize={}", awardedPrizesAtOnce, awardedPrize);
                }
            } else {
                awardedPrize.incrementHighFrequencyAwardedCount();
            }
        } else {
            awardedPrize.incrementHighFrequencyAwardedCount();
        }
    }

    private AwardedPrize getOrCreateAwardedPrize(long prizeId, long awardId, long awardTime, PromoCampaignMember member) {
        AwardedPrize awardedPrize = member.getAwardedPrizes().stream()
                .filter(existedPrize -> existedPrize.getPromoPrizeId() == prizeId)
                .findFirst()
                .orElseGet(() -> {
                    AwardedPrize createdPrize = new AwardedPrize(awardId, prizeId, awardTime);
                    member.addAwardedPrize(createdPrize);
                    return createdPrize;
                });
        awardedPrize.addNotSendedNotificationId(awardId);
        awardedPrize.setAwardDate(awardTime);
        return awardedPrize;
    }
}
