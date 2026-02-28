package com.abs.casino.promo.events.process;

import com.abs.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.*;
import org.apache.logging.log4j.LogManager;

/**
 * User: flsh
 * Date: 20.09.2019.
 */
public class MqEndRoundEventQualifier implements ParticipantEventQualifier<MqEndRoundEvent> {
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(MqEndRoundEventQualifier.class);

    private final ICurrencyRateManager currencyRateManager;

    public MqEndRoundEventQualifier(ICurrencyRateManager currencyRateManager) {
        this.currencyRateManager = currencyRateManager;
    }

    @Override
    public SignificantEventType getEventType() {
        return SignificantEventType.MQ_END_ROUND;
    }

    @Override
    public boolean qualifyEvent(MqEndRoundEvent event, IPrize campaignPrize, DesiredPrize desiredPrize, PromoCampaignMember member, IPromoCampaign campaign,
                                boolean updateMember) throws CommonException {
        if (!(campaignPrize instanceof TournamentPrize)) {
            LOG.error("campaignPrize must be TournamentPrize, campaign={}", campaign);
            return false;
        }
        TournamentPrize tournamentPrize = (TournamentPrize) campaignPrize;
        long betAmount = event.getBetAmount();
        if (tournamentPrize.getEventQualifier() instanceof MaxPerformanceEventQualifier) {
            MaxPerformanceEventQualifier maxPerformanceEventQualifier = (MaxPerformanceEventQualifier)
                    tournamentPrize.getEventQualifier();
            if (maxPerformanceEventQualifier.isNotCountWeaponBoxPurchases()) {
                betAmount = event.getBetAmount() - event.getPurchaseSpecialWeaponAmount();
            }
        }
        if (betAmount > 0) {
            PlayerBetEvent betEvent = new PlayerBetEvent(event.getGameId(), event.getEventDate(), event.getAccountId(), event.getAccountExternalId(),
                    betAmount, event.getCurrency());
            boolean betQualified = campaignPrize.qualifyBet(member, desiredPrize, betEvent, currencyRateManager, campaign.getBaseCurrency());
            LOG.debug("qualifyEvent: betQualified={}", betQualified);
            if (betQualified) {
                PlayerWinEvent winEvent = new PlayerWinEvent(event.getGameId(), event.getEventDate(), event.getAccountId(), event.getAccountExternalId(),
                        event.getWinAmount(), event.getCurrency());
                boolean winQualified = campaignPrize.qualifyWin(campaign, member, desiredPrize, winEvent, currencyRateManager, campaign.getBaseCurrency());
                LOG.debug("qualifyEvent: winQualified={}", winQualified);
                return winQualified;
            }
            return false;
        }
        return false;
    }

}

