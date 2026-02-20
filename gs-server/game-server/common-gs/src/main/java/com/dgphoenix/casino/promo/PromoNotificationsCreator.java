package com.dgphoenix.casino.promo;

import com.dgphoenix.casino.common.promo.*;
import com.dgphoenix.casino.common.promo.messages.server.notifications.PromoNotification;
import com.dgphoenix.casino.common.promo.messages.server.notifications.PromoStatusChanged;
import com.dgphoenix.casino.common.promo.messages.server.notifications.prizes.PrizeWonNotification;
import com.dgphoenix.casino.common.promo.messages.server.notifications.prizes.TicketsWon;
import com.dgphoenix.casino.common.promo.messages.server.notifications.tournament.LeaderBoard;
import com.dgphoenix.casino.common.promo.messages.server.notifications.tournament.TournamentPlace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * Created by vladislav on 4/4/17.
 */
public class PromoNotificationsCreator {
    private static final Logger LOG = LogManager.getLogger(PromoNotificationsCreator.class);

    private final IPromoCampaignManager promoCampaignManager;
    private final TournamentsManager tournamentsManager;

    public PromoNotificationsCreator(IPromoCampaignManager promoCampaignManager, TournamentsManager tournamentsManager) {
        this.promoCampaignManager = promoCampaignManager;
        this.tournamentsManager = tournamentsManager;
    }

    public List<PromoNotification> getNotifications(PromoCampaignMember promoCampaignMember,
                                                    Set<PromoNotificationType> notificationsTypes) {
        List<PromoNotification> notifications = new LinkedList<>();
        long campaignId = promoCampaignMember.getCampaignId();
        for (PromoNotificationType notificationType : notificationsTypes) {
            switch (notificationType) {
                case PRIZE_WON:
                    notifications.addAll(getPrizeWonNotifications(campaignId, promoCampaignMember));
                    break;
                case TOURNAMENT_INFO:
                    long accountId = promoCampaignMember.getAccountId();
                    notifications.addAll(getTournamentInfo(campaignId, accountId));
                    break;
                case CAMPAIGN_STATUS_CHANGED:
                    PromoNotification notification = getCampaignStatusNotification(campaignId);
                    if (notification != null) {
                        notifications.add(notification);
                    }
                    break;
                default:
                    LOG.warn("Unknown notification type: {}", notificationType);
                    break;
            }
        }
        return notifications;
    }

    public List<PromoNotification> getTournamentNotifications(long campaignId, long accountId) {
        return getTournamentInfo(campaignId, accountId);
    }

    private List<PromoNotification> getPrizeWonNotifications(long campaignId, PromoCampaignMember promoCampaignMember) {
        List<PromoNotification> notifications = new LinkedList<>();
        IPromoCampaign promoCampaign = null;
        for (AwardedPrize awardedPrize : promoCampaignMember.getAwardedPrizes()) {
            if (isNotEmpty(awardedPrize.getUnsentNotificationIds())) {
                if (promoCampaign == null) {
                    promoCampaign = promoCampaignManager.getPromoCampaign(campaignId);
                }

                long prizeId = awardedPrize.getPromoPrizeId();
                IPrize prize = promoCampaign.getPrize(prizeId);

                Set<Long> unsentNotificationsIds = awardedPrize.getUnsentNotificationIds();
                for (Long unsentNotificationId : unsentNotificationsIds) {
                    PrizeWonNotification wonMessage = prize.getWonMessage();
                    wonMessage.setIdentifiers(unsentNotificationId, campaignId);
                    if (wonMessage instanceof TicketsWon) {
                        int awardedCount = awardedPrize.getHighFrequencyAwardedCount();
                        TicketsWon ticketsWon = (TicketsWon) wonMessage;
                        ticketsWon.setTotalTickets(awardedCount);
                        ticketsWon.setWonTickets(awardedPrize.getUsendedNotificationsAwardedCount(unsentNotificationId));
                    }
                    notifications.add(wonMessage);
                }
            }
        }
        return notifications;
    }

    private List<PromoNotification> getTournamentInfo(long campaignId, long accountId) {
        List<PromoNotification> notifications = new LinkedList<>();
        IPromoCampaign promoCampaign = promoCampaignManager.getPromoCampaign(campaignId);
        if (promoCampaign.getTemplate().getPromoType().isTournamentLogic()) {
            LeaderBoard leaderBoard = tournamentsManager.getLeaderBoardForCampaign(campaignId);
            if (leaderBoard != null) {
                notifications.add(leaderBoard);
            }
            String place = tournamentsManager.getPlayerPlaceInTournament(campaignId, accountId);
            if (place != null) {
                TournamentPlace tournamentPlace = new TournamentPlace(campaignId, place);
                notifications.add(tournamentPlace);
            } else {
                LOG.debug("notifyPlacesUpdated: place for account {} not found", accountId);
            }
        }
        return notifications;
    }

    private PromoNotification getCampaignStatusNotification(long campaignId) {
        IPromoCampaign promoCampaign = promoCampaignManager.getPromoCampaign(campaignId);
        if (promoCampaign != null) {
            Status status = promoCampaign.getStatus();
            if (status != Status.STARTED) {
                return new PromoStatusChanged(campaignId, status);
            }
        }
        return null;
    }
}
