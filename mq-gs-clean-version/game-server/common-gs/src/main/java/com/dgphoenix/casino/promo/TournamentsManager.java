package com.dgphoenix.casino.promo;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.*;
import com.dgphoenix.casino.common.promo.messages.server.notifications.tournament.LeaderBoard;
import com.dgphoenix.casino.promo.persisters.CassandraTournamentRankPersister;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by vladislav on 3/20/17.
 */
public class TournamentsManager {
    private static final Logger LOG = LogManager.getLogger(TournamentsManager.class);

    private static final int MAX_LEADER_BOARD_SIZE = 100;
    private static final long DELAY_BEFORE_UPDATE_PLACES = TimeUnit.SECONDS.toMillis(30);
    private static final long UPDATE_PLACES_INTERVAL = TimeUnit.SECONDS.toMillis(60);

    private final IPromoCampaignManager promoCampaignManager;
    private final CassandraTournamentRankPersister tournamentRankPersister;
    private final ScheduledExecutorService executor;

    private final List<ITournamentsListener> tournamentsListeners = Lists.newCopyOnWriteArrayList();
    private final Map<Long, Map<Long, String>> tournamentsPlayersPlaces = new ConcurrentHashMap<>();
    private final Map<Long, LeaderBoard> tournamentsLeaderBoards = new ConcurrentHashMap<>();
    private volatile boolean initialized;
    private ScheduledFuture<?> scheduledFuture;

    public TournamentsManager(IPromoCampaignManager promoCampaignManager,
                              CassandraPersistenceManager cassandraPersistenceManager,
                              ScheduledExecutorService executor) {
        this.promoCampaignManager = promoCampaignManager;
        this.tournamentRankPersister = cassandraPersistenceManager.getPersister(CassandraTournamentRankPersister.class);
        this.executor = executor;
    }

    @PostConstruct
    private void init() {
        scheduledFuture = executor.scheduleWithFixedDelay(new PlacesUpdater(),
                DELAY_BEFORE_UPDATE_PLACES, UPDATE_PLACES_INTERVAL, TimeUnit.MILLISECONDS);
        initialized = true;
    }

    @PreDestroy
    private void shutdown() {
        scheduledFuture.cancel(true);
        initialized = false;
    }

    public void registerTournamentsListener(ITournamentsListener tournamentsListener) {
        tournamentsListeners.add(tournamentsListener);
    }

    private void notifyPlacesUpdated(long campaignId) {
        for (ITournamentsListener tournamentsListener : tournamentsListeners) {
            tournamentsListener.notifyPlacesUpdated(campaignId);
        }
    }

    public LeaderBoard getLeaderBoardForCampaign(long campaignId) {
        return tournamentsLeaderBoards.get(campaignId);
    }

    public String getPlayerPlaceInTournament(long campaignId, long accountId) {
        Map<Long, String> playersPlaces = tournamentsPlayersPlaces.get(campaignId);
        return playersPlaces != null
                ? playersPlaces.get(accountId)
                : null;
    }

    private class PlacesUpdater implements Runnable {
        @Override
        public void run() {
            LOG.debug("Start task");
            try {
                updatePlaces();
            } catch (Throwable e) {
                LOG.error("Error during updating ranks", e);
            }
        }

        private void updatePlaces() throws CommonException {
            LOG.debug("PlacesUpdater: updatePlaces started");
            Set<IPromoCampaign> promoCampaigns = promoCampaignManager.getPromoCampaigns(null, null,
                    Status.STARTED, null);
            for (IPromoCampaign promoCampaign : promoCampaigns) {
                if (!initialized) {
                    return;
                }

                IPromoTemplate<?,?> promoTemplate = promoCampaign.getTemplate();
                if (promoTemplate.getPromoType().isTournamentLogic()) {
                    LOG.debug("PlacesUpdater: process campaign, id = {}", promoCampaign.getId());

                    ITournamentPromoTemplate tournamentTemplate = (ITournamentPromoTemplate) promoTemplate;
                    TournamentRankQualifier rankQualifier = tournamentTemplate.getRankQualifier();

                    long campaignId = promoCampaign.getId();
                    Multimap<String, TournamentMemberRank> ranks = tournamentRankPersister
                            .getByCampaign(campaignId, rankQualifier);
                    LeaderBoard leaderBoard = new LeaderBoard(campaignId);
                    Map<Long, String> playersPlaces = new ConcurrentHashMap<>();
                    for (Map.Entry<String, Collection<TournamentMemberRank>> rankEntry : ranks.asMap().entrySet()) {
                        String place = rankEntry.getKey();
                        Collection<TournamentMemberRank> ranksInfos = rankEntry.getValue();
                        for (TournamentMemberRank memberRank : ranksInfos) {
                            if (leaderBoard.getSize() <= MAX_LEADER_BOARD_SIZE) {
                                leaderBoard.addLeader(place, memberRank.getNickName(), memberRank.getScore());
                            }
                            playersPlaces.put(memberRank.getAccountId(), place);
                        }
                    }

                    tournamentsLeaderBoards.put(campaignId, leaderBoard);
                    tournamentsPlayersPlaces.put(campaignId, playersPlaces);
                    notifyPlacesUpdated(campaignId);
                }
            }
        }
    }
}
