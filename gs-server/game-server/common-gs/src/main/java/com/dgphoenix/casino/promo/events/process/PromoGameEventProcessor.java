package com.abs.casino.promo.events.process;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.*;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.gs.managers.dblink.IGameDBLink;
import com.abs.casino.gs.managers.game.event.IGameEvent;
import com.abs.casino.gs.managers.game.event.IGameEventProcessor;
import com.dgphoenix.casino.promo.TournamentRanksExtractor;
import com.abs.casino.promo.persisters.CassandraTournamentRankPersister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * User: flsh
 * Date: 29.11.16.
 */
public class PromoGameEventProcessor implements IGameEventProcessor {
    private static final Logger LOG = LogManager.getLogger(PromoGameEventProcessor.class);

    private final IGameEventProcessor innerProcessor;
    private final IPromoCampaignManager campaignManager;
    private final ParticipantEventProcessor participantEventProcessor;
    //key is campaignId
    private final Map<Long, Set<SignificantEventType>> significantEvents;
    private final CassandraTournamentRankPersister rankPersister;

    public PromoGameEventProcessor(IGameEventProcessor innerProcessor, IPromoCampaignManager campaignManager,
                                   ParticipantEventProcessor participantEventProcessor, Map<Long, Set<SignificantEventType>> significantEvents,
                                   CassandraPersistenceManager persistenceManager) {
        this.innerProcessor = innerProcessor;
        this.campaignManager = campaignManager;
        this.participantEventProcessor = participantEventProcessor;
        this.significantEvents = significantEvents;
        this.rankPersister = persistenceManager.getPersister(CassandraTournamentRankPersister.class);
    }

    @Override
    public void process(IGameEvent gameEvent, IGameDBLink dbLink) {
        long now = System.currentTimeMillis();
        if (gameEvent instanceof IParticipantEvent) {
            LOG.debug("Received event: {}", gameEvent);
            IParticipantEvent promoEvent = (IParticipantEvent) gameEvent;
            SignificantEventType eventType = promoEvent.getType();
            for (Map.Entry<Long, Set<SignificantEventType>> campaignAndEvents : significantEvents.entrySet()) {
                Long campaignId = campaignAndEvents.getKey();
                setAdditionalParamsToEvent(promoEvent, dbLink, campaignId);
                Set<SignificantEventType> significantEvents = campaignAndEvents.getValue();
                if (significantEvents.contains(eventType)) {
                    try {
                        IPromoCampaign campaign = campaignManager.getPromoCampaign(campaignId);
                        if (campaign.isActual(promoEvent.getGameId())) {
                            participantEventProcessor.process(promoEvent, campaign, dbLink);
                        } else {
                            LOG.info("Campaign {} is not actual anymore, skip processing", campaignId);
                        }
                    } catch (CommonException e) {
                        LOG.error("Cannot process event: {}", promoEvent, e);
                    }
                }
            }
        }
        if (innerProcessor != null && dbLink != null) {
            innerProcessor.process(gameEvent, dbLink);
        }
        StatisticsManager.getInstance().updateRequestStatistics("PromoGameEventProcessor: process",
                System.currentTimeMillis() - now);
    }

    private void setAdditionalParamsToEvent(IParticipantEvent promoEvent, IGameDBLink dbLink, Long campaignId) {
        AbstractParticipantEvent event = (AbstractParticipantEvent) promoEvent;
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        TournamentMemberRanks ranks = TournamentRanksExtractor.extractRanksFromTD(transactionData);
        Iterator<TournamentMemberRank> rankIterator = ranks.getRanks().iterator();
        TournamentMemberRank memberRank = null;
        while (rankIterator.hasNext()) {
            TournamentMemberRank rank = rankIterator.next();
            if (rank.getCampaignId() == campaignId) {
                memberRank = rank;
                break;
            }
        }
        if (memberRank == null) {
            memberRank = rankPersister.getForAccount(campaignId, promoEvent.getAccountId());
        }
        event.setTournamentMemberRank(memberRank);
        event.setRoundId(dbLink == null ? null : dbLink.getRoundId());
    }
}
