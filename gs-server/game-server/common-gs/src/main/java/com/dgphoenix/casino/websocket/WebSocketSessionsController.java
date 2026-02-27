package com.abs.casino.websocket;

import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.PromoCampaignMember;
import com.dgphoenix.casino.common.promo.PromoCampaignMemberInfos;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.common.web.statistics.IStatisticsGetter;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerMessage;
import com.abs.casino.promo.messages.handlers.IMessageHandler;
import com.abs.casino.promo.messages.handlers.MessagesHandlersFactory;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by vladislav on 12/5/16.
 */
public class WebSocketSessionsController implements IWebSocketSessionsController {
    private static final Logger LOG = LogManager.getLogger(WebSocketSessionsController.class);
    private static final Gson GSON = new Gson();
    private static final String MESSAGE_TYPE_DELIMITER = "=";
    private static final int CLOSE_NOT_ACTUAL_SESSION = 3000;

    private final List<IWebSocketSessionsListener> sessionsListeners = Lists.newCopyOnWriteArrayList();
    private final Map<PlayerKey, SessionWrapper> sessionsByPlayers = new ConcurrentHashMap<>(256);
    private final MessagesHandlersFactory messagesHandlersFactory;
    private final ScheduledExecutorService keeperExecutor;
    private volatile boolean initialized;
    private ScheduledFuture<?> scheduledFuture;

    public WebSocketSessionsController(MessagesHandlersFactory messagesHandlersFactory, ScheduledExecutorService keeperExecutor) {
        this.messagesHandlersFactory = messagesHandlersFactory;
        this.keeperExecutor = keeperExecutor;
    }

    @PostConstruct
    private void init() {
        WebSocketImpl.registerSessionsController(this);
        scheduledFuture = keeperExecutor.scheduleWithFixedDelay(new WebSocketSessionsKeeper(), 30, 30, TimeUnit.SECONDS);
        initialized = true;
        StatisticsManager.getInstance().registerStatisticsGetter(getClass().getSimpleName(),
                () -> "sessions count=" + sessionsByPlayers.size());
    }

    @PreDestroy
    private void shutdown() {
        scheduledFuture.cancel(true);
        initialized = false;
    }

    @Override
    public void sendMessage(String sessionId, ServerMessage message) {
        PlayerKey playerKey = PlayerKey.composeFromSessionId(sessionId);
        SessionWrapper session = sessionsByPlayers.get(playerKey);
        if (!isSessionOnline(session)) {
            return;
        }
        String currentSessionId = session.getPlayerSessionId();
        if (!currentSessionId.equals(sessionId)) {
            return;
        }

        String fullMessage = message.getClass().getSimpleName() + "=" + GSON.toJson(message);
        LOG.debug("sendMessage: sessionId = {}, message = {}", sessionId, fullMessage);
        WebSocketMessageCallback webSocketMessageCallback = new WebSocketMessageCallback(session, sessionId, fullMessage);
        session.getRemote().sendString(fullMessage, webSocketMessageCallback);
    }

    private boolean isSessionOnline(Session session) {
        return session != null && session.isOpen();
    }

    @Override
    public void openSession(SessionWrapper session, String sessionId) throws CommonException {
        checkArgument(initialized, "Server is being shut down");

        SessionHelper.getInstance().lock(sessionId);
        try {
            SessionHelper.getInstance().openSession();

            ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
            SessionInfo playerSession = transactionData.getPlayerSession();
            if (playerSession == null || !playerSession.getSessionId().equals(sessionId)) {
                LOG.warn("openSession: session {} expired, close webSocket session", sessionId);
                session.close(StatusCode.NORMAL, "Session expired");
                return;
            }

            AccountInfo accountInfo = transactionData.getAccount();
            checkNotNull(accountInfo, "AccountInfo can't be null on open session, sessionId = {}", sessionId);

            int bankId = accountInfo.getBankId();
            String externalId = accountInfo.getExternalId();
            PlayerKey playerKey = new PlayerKey(bankId, externalId);
            if (!acceptNewSession(playerKey, session, sessionId)) {
                return;
            }

            PromoCampaignMemberInfos promoMemberInfos = transactionData.getPromoMemberInfos();
            if (promoMemberInfos != null && !promoMemberInfos.getPromoMembers().isEmpty()) {
                int currentServerId = GameServer.getInstance().getServerId();
                for (PromoCampaignMember campaignMember : promoMemberInfos.getPromoMembers().values()) {
                    campaignMember.setLastEnteredServerId(currentServerId);
                }
                SessionHelper.getInstance().commitTransaction();
            }

            notifySessionIsOpen(sessionId, session);

            SessionHelper.getInstance().markTransactionCompleted();
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }

        LOG.debug("openSession: session opened, sessionId = {}, sessions count = {}",
                sessionId, sessionsByPlayers.size());
    }

    private boolean acceptNewSession(PlayerKey playerKey, SessionWrapper newSession, String sessionId) {
        boolean accepted = false;
        boolean sessionIsActual = true;
        while (sessionIsActual && !accepted) {
            SessionWrapper existSession = sessionsByPlayers.putIfAbsent(playerKey, newSession);
            if (existSession != null) {
                if (existSession.getCreationTime() < newSession.getCreationTime()) {
                    sessionsByPlayers.remove(playerKey, existSession);
                    String existSessionId = newSession.getPlayerSessionId();
                    if (existSessionId.equals(sessionId)) {
                        existSession.close(CLOSE_NOT_ACTUAL_SESSION, "Close not actual webSocket session");
                    } else {
                        existSession.close(StatusCode.NORMAL, "Session expired");
                    }
                } else {
                    LOG.error("Attempting to open webSocket session which is already not actual, close. " +
                            "sessionId = {}, session = {}", sessionId, newSession);
                    newSession.close(CLOSE_NOT_ACTUAL_SESSION, "WebSocket session is already not actual");
                    sessionIsActual = false;
                }
            } else {
                accepted = true;
            }
        }
        return accepted;
    }

    @Override
    public void closeSession(SessionWrapper session, String sessionId, int status, String reason) {
        PlayerKey playerKey = PlayerKey.composeFromSessionId(sessionId);
        sessionsByPlayers.remove(playerKey, session);
        notifySessionIsClosed(sessionId);
        LOG.debug("closeSession: session closed, sessionId = {}, session = {}, status = {}, reason = {}",
                sessionId, session, status, reason);
    }

    @Override
    public void closeWebSocketForPlayerSession(String sessionId) {
        PlayerKey playerKey = PlayerKey.composeFromSessionId(sessionId);
        SessionWrapper session = sessionsByPlayers.get(playerKey);
        if (session != null && Objects.equals(sessionId, session.getPlayerSessionId())) {
            LOG.debug("Found session, close");
            session.close(StatusCode.NORMAL, "Player session closed");
        }
    }

    @Override
    public void processMessage(String sessionId, String message) throws CommonException {
        String[] messageParts = message.split(MESSAGE_TYPE_DELIMITER);
        checkArgument(messageParts.length == 2, "Message must have the format Type=params");
        String messageType = messageParts[0];
        String messageParams = messageParts[1];

        IMessageHandler<?> messageHandler = messagesHandlersFactory.getHandler(messageType);
        if (messageHandler != null) {
            messageHandler.handleMessage(this, sessionId, messageParams);
        } else {
            throw new IllegalArgumentException("Unsupported message type = " + messageType);
        }
    }

    @Override
    public void registerSessionsListener(IWebSocketSessionsListener sessionsListener) {
        sessionsListeners.add(sessionsListener);
    }

    private void notifySessionIsOpen(String sessionId, SessionWrapper session) throws CommonException {
        for (IWebSocketSessionsListener sessionsListener : sessionsListeners) {
            sessionsListener.notifyWebSocketForSessionIsOpen(sessionId, session);
        }
    }

    private void notifySessionIsClosed(String sessionId) {
        for (IWebSocketSessionsListener sessionsListener : sessionsListeners) {
            sessionsListener.notifyWebSocketForSessionIsClosed(sessionId);
        }
    }

    private class WebSocketSessionsKeeper implements Runnable {
        private volatile long pingsCount;

        public WebSocketSessionsKeeper() {
            StatisticsManager.getInstance().registerStatisticsGetter(WebSocketSessionsKeeper.class.getSimpleName(),
                    new IStatisticsGetter() {
                        @Override
                        public String getStatistics() {
                            return "pingsCount=" + pingsCount;
                        }
                    });
        }

        @Override
        public void run() {
            LOG.debug("Start task");
            for (Entry<PlayerKey, SessionWrapper> playerAndSession : sessionsByPlayers.entrySet()) {
                if (!initialized) {
                    return;
                }
                Session session = playerAndSession.getValue();
                try {
                    keepAlive(session);
                    LOG.debug("End task");
                } catch (Throwable e) {
                    LOG.error("WebSocketSessionsKeeper: error during keepAlive", e);
                }
            }
        }

        private void keepAlive(Session session) throws CommonException, IOException {
            if (!isSessionOnline(session)) {
                return;
            }
            session.getRemote().sendPing(null); //to keep connection alive
            pingsCount++;
        }
    }

    private static class PlayerKey {
        public final long bankId;
        public final String externalUserId;

        public PlayerKey(long bankId, String externalUserId) {
            this.bankId = bankId;
            this.externalUserId = externalUserId;
        }

        public static PlayerKey composeFromSessionId(String sessionId) {
            Pair<Integer, String> bankAndExtUsrId = StringIdGenerator.extractBankAndExternalUserId(sessionId);
            return new PlayerKey(bankAndExtUsrId.getKey(), bankAndExtUsrId.getValue());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PlayerKey playerKey = (PlayerKey) o;

            if (bankId != playerKey.bankId) return false;
            return !(externalUserId != null ? !externalUserId.equals(playerKey.externalUserId) : playerKey.externalUserId != null);
        }

        @Override
        public int hashCode() {
            int result = (int) (bankId ^ (bankId >>> 32));
            result = 31 * result + (externalUserId != null ? externalUserId.hashCode() : 0);
            return result;
        }
    }
}
