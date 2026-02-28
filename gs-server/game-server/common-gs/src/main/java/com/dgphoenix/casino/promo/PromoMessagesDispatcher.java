package com.abs.casino.promo;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.exception.CannotLockException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.*;
import com.abs.casino.common.promo.messages.server.notifications.PromoNotification;
import com.abs.casino.common.promo.messages.server.notifications.PromoStatusChanged;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.dgphoenix.casino.common.util.Pair;
import com.abs.casino.common.util.string.StringIdGenerator;
import com.abs.casino.common.web.statistics.IStatisticsGetter;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.websocket.IWebSocketSessionsController;
import com.abs.casino.websocket.SessionWrapper;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by vladislav on 12/1/16.
 */
public class PromoMessagesDispatcher implements IPromoMessagesDispatcher, ITournamentsListener {
    private static final Logger LOG = LogManager.getLogger(PromoMessagesDispatcher.class);

    private final IWebSocketSessionsController webSocketController;
    private final IPromoCampaignManager promoCampaignManager;
    private final PromoNotificationsCreator promoNotificationsCreator;

    private final ConcurrentHashMap<Long, Set<String>> sessionsByCampaigns = new ConcurrentHashMap<>();
    private final ThreadPoolExecutor sendersExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    private final ConcurrentHashMap<String, NotificationsSender> activeSenders = new ConcurrentHashMap<>();
    private final AtomicInteger sendersPeek = new AtomicInteger();
    private volatile boolean initialized;

    public PromoMessagesDispatcher(IWebSocketSessionsController webSocketController,
                                   IPromoCampaignManager promoCampaignManager,
                                   TournamentsManager tournamentsManager,
                                   PromoNotificationsCreator promoNotificationsCreator) {
        this.webSocketController = webSocketController;
        this.promoCampaignManager = promoCampaignManager;
        this.promoNotificationsCreator = promoNotificationsCreator;
        tournamentsManager.registerTournamentsListener(this);
        this.webSocketController.registerSessionsListener(this);
        StatisticsManager.getInstance().registerStatisticsGetter(getClass().getSimpleName(), new IStatisticsGetter() {
            @Override
            public String getStatistics() {
                StringBuilder statBuilder = new StringBuilder("Sessions by campaigns: ");
                statBuilder.append("campaigns = ").append(sessionsByCampaigns.size());
                for (Map.Entry<Long, Set<String>> campaignAndSessions : sessionsByCampaigns.entrySet()) {
                    long campaignId = campaignAndSessions.getKey();
                    Set<String> sessions = campaignAndSessions.getValue();
                    statBuilder.append(", campaign = ").append(campaignId)
                            .append(" sessions = ").append(sessions.size());
                }
                statBuilder.append(", sendersPeek: ").append(sendersPeek.get());
                return statBuilder.toString();
            }
        });
    }

    @PostConstruct
    private void init() {
        initialized = true;
    }

    @PreDestroy
    private void shutdown() {
        initialized = false;
        ExecutorUtils.shutdownService(getClass().getSimpleName(), sendersExecutor, TimeUnit.SECONDS.toMillis(30));
    }

    @Override
    public void sendPromoNotifications(String sessionId, long campaignId,
                                       Set<PromoNotificationType> notificationsTypes) {
        long now = System.currentTimeMillis();
        LOG.debug("sendPromoNotifications: sessionId = {}, campaign = {}", sessionId, campaignId);
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        PromoCampaignMemberInfos promoMemberInfos = transactionData.getPromoMemberInfos();
        if (promoMemberInfos == null) {
            LOG.debug("sendPromoNotifications: promoMemberInfos is null, sessionId = {}", sessionId);
            return;
        }
        PromoCampaignMember promoCampaignMember = promoMemberInfos.get(campaignId);
        if (promoCampaignMember == null) {
            LOG.debug("sendPromoNotifications: campaignMember is null, sessionId = {}, campaignId = {}",
                    sessionId, campaignId);
            return;
        }

        List<PromoNotification> notifications = promoNotificationsCreator
                .getNotifications(promoCampaignMember, notificationsTypes);
        for (PromoNotification notification : notifications) {
            webSocketController.sendMessage(sessionId, notification);
        }
        StatisticsManager.getInstance().updateRequestStatistics("sendPromoNotifications",
                System.currentTimeMillis() - now);
    }

    @Override
    public void sendPromoNotificationsAsync(String sessionId, long campaignId,
                                            Set<PromoNotificationType> notificationsTypes) {
        NotificationsSender activeSender = activeSenders.get(sessionId);
        if (activeSender == null) {
            NotificationsSender newSender = new NotificationsSender(sessionId, campaignId, notificationsTypes);
            activeSender = activeSenders.putIfAbsent(sessionId, newSender);
            if (activeSender == null) {
                try {
                    sendersExecutor.submit(newSender);
                } catch (Exception e) {
                    LOG.error("Error on sender submission", e);
                    activeSenders.remove(sessionId);
                }
            }
        }
        if (activeSender != null) {
            activeSender.addNotifications(campaignId, notificationsTypes);
        }

        int activeSenders = sendersExecutor.getActiveCount();
        int currentSendersPeek = sendersPeek.get();
        while (currentSendersPeek < activeSenders && !sendersPeek.compareAndSet(currentSendersPeek, activeSenders)) {
            currentSendersPeek = sendersPeek.get();
        }
    }

    @Override
    public void markPrizesAsNotifiedAbout(String sessionId, Map<Long, Collection<Long>> prizesIdsByCampaigns)
            throws CommonException {
        boolean transactionAlreadyStarted = SessionHelper.getInstance().isTransactionStarted();
        if (!transactionAlreadyStarted) {
            SessionHelper.getInstance().lock(sessionId);
        }
        try {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().openSession();
            }

            boolean hasChanges = false;
            for (Map.Entry<Long, Collection<Long>> campaignPrizes : prizesIdsByCampaigns.entrySet()) {
                long campaignId = campaignPrizes.getKey();
                Collection<Long> prizesIds = campaignPrizes.getValue();
                hasChanges = markPrizesAsNotified(sessionId, campaignId, prizesIds);
            }

            if (hasChanges) {
                SessionHelper.getInstance().commitTransaction();
            }
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().markTransactionCompleted();
            }
        } finally {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().clearWithUnlock();
            }
        }
    }

    private boolean markPrizesAsNotified(String sessionId, long campaignId, Collection<Long> prizesIds) {
        boolean markedSomething = false;
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        PromoCampaignMember campaignMember = transactionData.getPromoMemberInfos().getPromoMembers().get(campaignId);
        if (campaignMember != null) {
            for (Long prizeId : prizesIds) {
                for (AwardedPrize awardedPrize : campaignMember.getAwardedPrizes()) {
                    if (awardedPrize.isNotificationNotSent(prizeId)) {
                        LOG.debug("markPrizesAsNotifiedAbout: prize is shown, mark as notified, " +
                                "sessionId = {}, campaignId = {}, prizeId = {}", sessionId, campaignId, prizesIds);
                        awardedPrize.setNotificationSent(prizeId);
                        markedSomething = true;
                    }
                }
            }
        }
        return markedSomething;
    }

    @Override
    public void notifyWebSocketForSessionIsOpen(String sessionId, SessionWrapper webSocketSession) {
        Set<Long> activePromoIds = webSocketSession.getActivePromoIds();
        if (CollectionUtils.isNotEmpty(activePromoIds)) {
            notifyPromoSessionIsOpen(sessionId, activePromoIds);
        }
    }

    @Override
    public void notifyWebSocketForSessionIsClosed(String sessionId) {
        for (Set<String> sessions : sessionsByCampaigns.values()) {
            sessions.remove(sessionId);
        }
    }

    @Override
    public void notifyPromoSessionIsOpen(String sessionId, Set<Long> activeCampaigns) {
        registerSessionInPromoCampaignsWithCheck(sessionId, activeCampaigns);
        for (Long campaignId : activeCampaigns) {
            sendPromoNotifications(sessionId, campaignId, EnumSet.allOf(PromoNotificationType.class));
        }
    }

    private void registerSessionInPromoCampaignsWithCheck(String sessionId, Set<Long> promoCampaignIds) {
        for (Long promoCampaignId : promoCampaignIds) {
            IPromoCampaign promoCampaign = promoCampaignManager.getPromoCampaign(promoCampaignId);
            if (promoCampaign.getStatus() == Status.STARTED) {
                Set<String> sessions = sessionsByCampaigns.get(promoCampaignId);
                if (sessions == null) {
                    Set<String> newSessionsContainer = Sets.newConcurrentHashSet();
                    sessions = sessionsByCampaigns.putIfAbsent(promoCampaignId, newSessionsContainer);
                    if (sessions == null) {
                        sessions = newSessionsContainer;
                    }
                }
                sessions.add(sessionId);
            } else {
                PromoStatusChanged statusChanged = new PromoStatusChanged(promoCampaignId, promoCampaign.getStatus());
                webSocketController.sendMessage(sessionId, statusChanged);
            }
        }
    }

    @Override
    public void notifyPromoCampaignCreated(long promoCampaignId) {

    }

    @Override
    public void notifyPromoCampaignStatusChanged(long campaignId, Status newStatus) {
        LOG.debug("notifyPromoCampaignStatusChanged: campaignId = {}, newStatus = {}", campaignId, newStatus);
        PromoStatusChanged statusChangedNotification = new PromoStatusChanged(campaignId, newStatus);
        Set<String> sessions = sessionsByCampaigns.get(campaignId);
        if (sessions != null) {
            for (String sessionId : sessions) {
                webSocketController.sendMessage(sessionId, statusChangedNotification);
            }
            if (newStatus != Status.STARTED) {
                sessionsByCampaigns.remove(campaignId);
            }
        }
    }

    @Override
    public void notifyPlacesUpdated(long campaignId) {
        LOG.debug("notifyPlacesUpdated: campaignId = {}", campaignId);
        Set<String> campaignSessions = sessionsByCampaigns.get(campaignId);
        if (campaignSessions != null) {
            for (String sessionId : campaignSessions) {
                Pair<Integer, String> bankAndExtId = StringIdGenerator.extractBankAndExternalUserId(sessionId);
                Integer bankId = bankAndExtId.getKey();
                String externalUserId = bankAndExtId.getValue();
                AccountInfo accountInfo;
                try {
                    accountInfo = AccountManager.getInstance().getByCompositeKey(bankId, externalUserId);
                } catch (CommonException e) {
                    LOG.error("Cannot get accountInfo to check position in the tournament, bankId={}, externalUserId={}",
                            bankId, externalUserId, e);
                    continue;
                }
                if (accountInfo != null) {
                    List<PromoNotification> notifications = promoNotificationsCreator
                            .getTournamentNotifications(campaignId, accountInfo.getId());
                    for (PromoNotification notification : notifications) {
                        webSocketController.sendMessage(sessionId, notification);
                    }
                }
            }
        }
    }

    private class NotificationsSender implements Runnable {
        private final String sessionId;
        private final Map<Long, Set<PromoNotificationType>> campaignsNotifications = new ConcurrentHashMap<>();

        public NotificationsSender(String sessionId, long campaignId, Set<PromoNotificationType> notificationsTypes) {
            this.sessionId = sessionId;
            addNotifications(campaignId, notificationsTypes);
        }

        public void addNotifications(long campaignId, Set<PromoNotificationType> notificationsTypes) {
            Set<PromoNotificationType> existNotifications = campaignsNotifications.get(campaignId);
            if (existNotifications == null) {
                Set<PromoNotificationType> newNotifications = Sets.newConcurrentHashSet(notificationsTypes);
                existNotifications = campaignsNotifications.putIfAbsent(campaignId, newNotifications);
            }
            if (existNotifications != null) {
                existNotifications.addAll(notificationsTypes);
            }
        }

        @Override
        public void run() {
            if (!initialized) {
                return;
            }
            long now = System.currentTimeMillis();
            LOG.debug("Start sending promo notifications, sessionId = {}", sessionId);
            try {
                SessionHelper.getInstance().lock(sessionId, 10000);
                try {
                    if (!initialized) {
                        return;
                    }
                    SessionHelper.getInstance().openSession();

                    ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
                    SessionInfo playerSession = transactionData.getPlayerSession();
                    GameSession gameSession = transactionData.getGameSession();
                    if (playerSession != null && playerSession.getSessionId().equals(sessionId) && gameSession != null) {
                        sendNotifications();
                    } else {
                        LOG.debug("Session expired, playerSession = {}, gameSession = {}",
                                playerSession, gameSession);
                    }

                    SessionHelper.getInstance().markTransactionCompleted();
                    activeSenders.remove(sessionId);
                } finally {
                    SessionHelper.getInstance().clearWithUnlock();
                }
            } catch (Throwable e) {
                LOG.error("Error during sending notifications, sessionId = {}", sessionId, e);
                if (e instanceof CannotLockException) {
                    LOG.error("Caused by failed lock attempt, resubmit");
                    try {
                        if (initialized) {
                            sendersExecutor.submit(this);
                        } else {
                            LOG.debug("Server is being shut down, reject submission");
                        }
                    } catch (Exception submitException) {
                        LOG.error("Cannot resubmit sender, sessionId = {}", sessionId, submitException);
                        activeSenders.remove(sessionId);
                    }
                } else {
                    activeSenders.remove(sessionId);
                }
            }
            StatisticsManager.getInstance().updateRequestStatistics("PromoMessagesDispatcher.NotificationsSender: run",
                    System.currentTimeMillis() - now);
        }

        private void sendNotifications() {
            for (Map.Entry<Long, Set<PromoNotificationType>> campaignAndNotifications : campaignsNotifications.entrySet()) {
                long campaignId = campaignAndNotifications.getKey();
                Set<PromoNotificationType> notifications = campaignAndNotifications.getValue();
                sendPromoNotifications(sessionId, campaignId, notifications);
            }
        }
    }
}
