package com.abs.casino.websocket.tournaments;

import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.transport.ITransportObject;
import com.dgphoenix.casino.common.transport.TObject;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper;
import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.KafkaHandlerException;
import com.abs.casino.promo.tournaments.ErrorCodes;
import com.dgphoenix.casino.promo.tournaments.handlers.TournamentMessageHandlersFactory;
import com.abs.casino.promo.tournaments.messages.BalanceUpdated;
import com.abs.casino.promo.tournaments.messages.Error;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkArgument;

public class TournamentWebSocketSessionsController {
    private static final Logger LOG = LogManager.getLogger(TournamentWebSocketSessionsController.class);
    private static final Gson GSON = GsonFactory.createGson();

    private final TournamentMessageHandlersFactory messageHandlersFactory;
    private final RemoteCallHelper remoteCallHelper;
    private final ScheduledExecutorService keeperExecutor;

    private final ConcurrentMap<String, ISocketClient> clients = new ConcurrentHashMap<>(256);
    private volatile boolean initialized;
    private ScheduledFuture<?> webSocketSessionsKeeperTask;
    private ScheduledFuture<?> balanceUpdaterTask;

    public TournamentWebSocketSessionsController(TournamentMessageHandlersFactory messageHandlersFactory,
                                                 RemoteCallHelper remoteCallHelper,
                                                 ScheduledExecutorService keeperExecutor) {
        this.messageHandlersFactory = messageHandlersFactory;
        messageHandlersFactory.addTournamentWebSocketMessageListener(
                new TournamentWebSocketMessageListener(this, remoteCallHelper));
        this.remoteCallHelper = remoteCallHelper;
        this.keeperExecutor = keeperExecutor;
    }

    @PostConstruct
    private void init() {
        TournamentWebSocket.registerSessionsController(this);
        webSocketSessionsKeeperTask = keeperExecutor.scheduleWithFixedDelay(new WebSocketSessionsKeeper(), 30, 60, TimeUnit.SECONDS);

        MQServiceHandler serviceHandler = ApplicationContextHelper.getApplicationContext().getBean(MQServiceHandler.class);

        if (serviceHandler != null) {
            balanceUpdaterTask = keeperExecutor.scheduleWithFixedDelay(new BalanceUpdater(serviceHandler), 30, 30, TimeUnit.SECONDS);
            serviceHandler.addTournamentWebSocketMessageListener(
                    new TournamentWebSocketMessageListener(this, remoteCallHelper));
        } else {
            LOG.error("init: Cannot start BalanceUpdater, serviceHandler not found");
        }
        initialized = true;
        StatisticsManager.getInstance().registerStatisticsGetter(getClass().getSimpleName(),
                () -> "clients count=" + clients.size());
    }

    @PreDestroy
    private void shutdown() {
        webSocketSessionsKeeperTask.cancel(true);
        if (balanceUpdaterTask != null) {
            balanceUpdaterTask.cancel(true);
        }
        initialized = false;
    }

    public void sendMessage(String sessionId, TObject message) {
        ISocketClient client = clients.get(sessionId);
        if (client != null) {
            client.sendMessage(message);
        }
    }

    public void openSession(ISocketClient client) {
        String sessionId = client.getSessionId();
        checkArgument(initialized, "Server is being shut down");
        checkArgument(!StringUtils.isTrimmedEmpty(sessionId), "Missed session id param");
        Session oldSession = clients.get(sessionId);
        if (oldSession != null && oldSession.isOpen()) {
            oldSession.close();
        }
        clients.put(sessionId, client);
        LOG.debug("client opened, sessionId={}, sessions count={}", sessionId, clients.size());
    }

    public void closeSession(String sessionId, int status, String reason) {
        ISocketClient client = clients.get(sessionId);
        if (client != null) {
            if (client.isOpen()) {
                client.close();
            }
            clients.remove(client.getSessionId());
            LOG.debug("client closed, sessionId={}, status={}, reason={}", client.getSessionId(), status, reason);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void processMessage(String sessionId, String message) {
        ISocketClient client = clients.get(sessionId);
        if (client == null) {
            LOG.error("Can't process, client not found, message={}", message);
            return;
        }
        TObject tMessage = deserialize(message);
        if (tMessage == null) {
            sendMessage(sessionId,
                    createErrorMessage(ErrorCodes.BAD_REQUEST, "Bad request: '" + message + "'", -1));
            LOG.error("Drop connection, can't parse message");
            closeSession(sessionId, StatusCode.BAD_PAYLOAD, "Bad request");
            return;
        }
        LOG.debug("processMessage: {}, sessionId={}", tMessage, sessionId);
        IMessageHandler handler = getHandler(tMessage.getClass());
        if (handler != null) {
            try {
                handler.handle(tMessage, client);
            } catch (Exception e) {
                LOG.error("Unexpected error", e);
            }
        } else {
            LOG.info("Handler for message of type {} not found", tMessage.getClass());
            client.sendMessage(createErrorMessage(ErrorCodes.BAD_REQUEST, "Bad request", tMessage.getRid()));
        }
    }

    public IMessageHandler<?> getHandler(Class<? extends ITransportObject> klass) {
        return messageHandlersFactory.getHandler(klass);
    }

    public Map<String, ISocketClient> getClients() {
        return clients;
    }

    private TObject deserialize(String message) {
        TObject object;
        try {
            object = GSON.fromJson(message, TObject.class);
        } catch (RuntimeException e) {
            LOG.debug("Cannot parse message: {}", message, e);
            throw e;
        }
        return object;
    }

    private TObject createErrorMessage(int code, String msg, int rid) {
        return new Error(code, msg, System.currentTimeMillis(), rid);
    }

    private boolean isSessionOnline(Session session) {
        return session != null && session.isOpen();
    }

    private class BalanceUpdater implements Runnable {
        private final MQServiceHandler serviceHandler;

        public BalanceUpdater(MQServiceHandler serviceHandler) {
            this.serviceHandler = serviceHandler;
        }

        @Override
        public void run() {
            LOG.debug("Start task");
            for (ISocketClient client : clients.values()) {
                if (!initialized) {
                    return;
                }
                try {
                    if (client instanceof TournamentClient && client.isOpen() && client.isConnected()) {
                        updateBalance((TournamentClient) client);
                    }
                } catch (Exception e) {
                    LOG.error("BalanceUpdater: cannot send balance update", e);
                }
            }
        }

        private void updateBalance(TournamentClient client) throws KafkaHandlerException {
            long balance = serviceHandler.getBalance(client.getSessionId(), GameMode.REAL.name());
            if (client.isOpen()) {
                client.sendMessage(new BalanceUpdated(balance));
            }
        }
    }

    private class WebSocketSessionsKeeper implements Runnable {
        private volatile long pingsCount;

        public WebSocketSessionsKeeper() {
            StatisticsManager.getInstance().registerStatisticsGetter(WebSocketSessionsKeeper.class.getSimpleName(),
                    () -> "pingsCount=" + pingsCount);
        }

        @Override
        public void run() {
            LOG.debug("Start task");
            for (ISocketClient client : clients.values()) {
                if (!initialized) {
                    return;
                }
                try {
                    keepAlive(client);
                } catch (Throwable e) {
                    LOG.error("WebSocketSessionsKeeper: error during keepAlive", e);
                }
            }
        }

        private void keepAlive(Session session) throws IOException {
            if (isSessionOnline(session)) {
                session.getRemote().sendPing(null);
                pingsCount++;
            }
        }
    }
}
