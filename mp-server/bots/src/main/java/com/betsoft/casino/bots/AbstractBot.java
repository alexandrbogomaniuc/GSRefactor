package com.betsoft.casino.bots;

import com.betsoft.casino.bots.handlers.IServerMessageHandler;
import com.betsoft.casino.bots.requests.IBotRequest;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.NewEnemies;
import com.betsoft.casino.mp.transport.NewEnemy;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IConfigurableWebSocketHandler;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public abstract class AbstractBot implements IBot {
    private static final String PACKAGE = "com.betsoft.casino.";
    protected static final Scheduler sch = Schedulers.newParallel("BOT_SCHEDULER", 1000);
    protected static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss SSS");

    protected final Logger LOG;
    protected final String sessionId;
    protected final IMessageSerializer serializer;
    protected final Function<Void, Integer> shutdownCallback;
    protected final Function<Void, Integer> startCallback;
    protected final String id;

    protected final Map<Integer, IBotRequest> requests = new HashMap<>();
    protected final Map<Class<? extends TObject>, IServerMessageHandler> serverMessageHandlers = new HashMap<>();

    protected final ReentrantLock lock = new ReentrantLock();

    protected String url;
    protected int serverId;
    protected int bankId;
    protected Stats stats;
    protected ISocketClient client;
    public boolean started = false;
    protected WebSocketClient webSocketClient;

    protected AbstractBot(String id, String url, int serverId, int bankId, String sessionId, IMessageSerializer serializer,
                       Function<Void, Integer> shutdownCallback, Function<Void, Integer> startCallback) {
        this.LOG = LogManager.getLogger(PACKAGE + this.getClass().getSimpleName() + "-" + id);
        this.id = id;
        this.url = url;
        this.serverId = serverId;
        this.bankId = bankId;
        this.sessionId = sessionId;
        this.serializer = serializer;
        this.shutdownCallback = shutdownCallback;
        this.startCallback = startCallback;
        registerServerMessageHandlers();
    }

    @Override
    public void start() {
        getLogger().debug("AbstractBot=>start: Starting bot with params ws='{}', sid='{}', server={}", url, sessionId, serverId);
        started = true;
        if (stats == null) {
            stats = new Stats();
        }
        requests.clear();
        webSocketClient = new ReactorNettyWebSocketClient();

        IConfigurableWebSocketHandler webSocketHandler = new IConfigurableWebSocketHandler() {
            @Override
            public Mono<Void> handle(WebSocketSession session) {
                try {
                    // Raise WS frame size limit
                    changeDefaultConfig(session, false, LOG);
                } catch (Exception e) {
                    LOG.error("handle: Exception", e);
                }

                Mono<Void> outbound = session.send(
                        Flux.create((FluxSink<WebSocketMessage> sink) ->
                                        createClient(session, sink, getBankId()), FluxSink.OverflowStrategy.BUFFER
                                )
                                .doFinally(s ->
                                        closeConnection(session)
                                )
                );

                Mono<Void> inbound = session.receive()
                        .doOnNext(message ->
                                processMessage(session, message))
                        .then();

                return Mono.when(outbound, inbound);
            }
        };

        //noinspection NullableProblems
        webSocketClient
                .execute(URI.create(url), webSocketHandler)
                .subscribe();

        startCallback.apply(null);
        getLogger().debug("Starting bot finished with params ws='{}', sid='{}', server={}", url, sessionId, serverId);
    }

    protected void closeConnection(WebSocketSession session) {
        getLogger().debug("closeConnection: {}", session.getId());
    }

    @Override
    public void stop() {
        if (started) {
            started = false;
            if (client != null) {
                FluxSink<WebSocketMessage> connection = client.getConnection();
                if(connection != null) {
                    connection.complete();
                }
            }
            if (client != null) {
                WebSocketSession webSocketSession = client.getSession();
                if(webSocketSession != null) {
                    try {
                        //noinspection ReactiveStreamsUnusedPublisher, close() always return Mono.empty()
                        getLogger().debug("stop: try stop webSocketSession: {}", webSocketSession);
                        webSocketSession.close().block();
                    } catch (Exception e) {
                        try {
                            //noinspection ReactiveStreamsUnusedPublisher, close() always return Mono.empty()
                            getLogger().debug("stop: try stop without block webSocketSession: {}, message:{}", webSocketSession, e.getMessage());
                            webSocketSession.close();
                        } catch (Exception error) {
                            getLogger().debug("stop: Cannot close connection", error);
                        }
                    }
                } else {
                    getLogger().debug("stop: webSocketSession is null");
                }
            } else {
                getLogger().debug("stop: client is null");
            }
            requests.clear();
            shutdownCallback.apply(null);
        }
    }

    @Override
    public void restart() {
        getLogger().debug("restart: start");
        stop();
        sleep(300).subscribe(t -> start());
        getLogger().debug("restart: end");
    }

    public Map<Integer, IBotRequest> getRequests() {
        return requests;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public Stats getStats() {
        return stats;
    }

    public String getId() {
        return id;
    }

    public void count(int key) {
        stats.count(key);
    }

    public void count(int key, int delta) {
        stats.count(key, delta);
    }

    @Override
    public int send(IBotRequest request) {
        int rid;
        lock.lock();
        try {
            if (client == null) {
                throw new NullPointerException("SocketClient is null, need start first");
            }
            getLogger().debug("send: Sending request: {}", request);
            rid = client.getRid();
            requests.put(rid, request);
            request.send(rid);
        } finally {
            lock.unlock();
        }
        return rid;
    }

    @Override
    public Mono<Long> sleep(int min, int max) {
        long time = RNG.nextInt(min, max);
        //getLogger().debug("Sleeping for {} ms", time);
        return Mono.delay(Duration.ofMillis(time), sch);
    }

    @Override
    public Mono<Long> sleep(long time) {
        return Mono.delay(Duration.ofMillis(time), sch).doOnError(t -> getLogger().error("sleep error", t));
    }

    protected abstract void registerServerMessageHandlers();

    protected abstract void sendInitialRequest();

    protected void createClient(WebSocketSession session, FluxSink<WebSocketMessage> sink, int bankId) {
        client = new SocketClient(session, sink, serializer, bankId, LOG);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            //nop
        }
        sendInitialRequest();
    }

    @SuppressWarnings("unchecked")
    protected void processMessage(WebSocketSession messageSession, WebSocketMessage message) {
        if (WebSocketMessage.Type.PING.equals(message.getType())) {
            client.sendMessage(new WebSocketMessage(WebSocketMessage.Type.PONG, message.getPayload()));
            return;
        }

        String clientSessionId = client == null || client.getSession() == null ? null : client.getSession().getId();
        String messageSessionId = messageSession.getId();
        //getLogger().debug("processMessage: client session.id={}, messageSession.id={}", clientSessionId, messageSessionId);

        if (messageSessionId.equals(clientSessionId)) {

            TObject response;
            try {
                response = serializer.deserialize(message);
            } catch (Exception e) {
                getLogger().error("processMessage: Cannot deserialize message={}, ignore", message, e);
                return;
            }

            if(response != null) {
                processMessage(response);
            } else {
                getLogger().error("processMessage: response is null for WebSocketSession.id {}", messageSessionId);
            }

        } else {
            getLogger().error("processMessage: WebSocketSession.id {} mismatch with clientSessionId {}",
                    messageSessionId, clientSessionId);
        }
    }

    public void processMessage(ITransportObject response) {
        //prevent very huge logging
        int rid = response.getRid();

        if (response instanceof NewEnemies || response instanceof NewEnemy) {
            getLogger().debug("processMessage: {} received message: {}, rid={}", this.getId(), response.getClassName(), rid);
        } else {
            getLogger().debug("processMessage: {}, received message: {}", this.getId(), response);
        }

        lock.lock();
        try {
            if (rid == -1) {
                IServerMessageHandler handler = serverMessageHandlers.get(response.getClass());
                if (handler != null) {

                    handler.handle(response);

                } else {
                    LOG.error("processMessage: unsupported server message: {}", response.getClass());
                }
            } else {
                IBotRequest handler = requests.get(rid);
                if (handler != null) {

                    handler.handle(response);

                    if (handler.isSingleResponse()) {
                        requests.remove(rid);
                    }

                    updateRequestStatistics(handler, response);

                } else if (response instanceof Error) {

                    Error errorResponse = (Error) response;
                    int code = errorResponse.getCode();
                    if (code == ErrorCodes.ROUND_NOT_STARTED || code == ErrorCodes.NOT_ENOUGH_BULLETS) {
                        getLogger().warn("processMessage: Unexpected message: {}", response);
                    } else {
                        getLogger().error("processMessage: Unexpected message: {}", response);
                    }

                } else {
                    getLogger().warn("processMessage: requestHandler not found, class={}, rid={}",
                            response.getClassName(), response.getRid());
                }

                if (requests.size() > 10) {
                    getLogger().debug("processMessage: Found many requests {}, try clear old", requests.size());

                    Set<Integer> oldRequests = new HashSet<>();
                    for (Map.Entry<Integer, IBotRequest> requestEntry : requests.entrySet()) {
                        IBotRequest botRequest = requestEntry.getValue();
                        Integer requestId = requestEntry.getKey();
                        if (botRequest.getElapsedTime() > 10000) {
                            LOG.warn("processMessage: Remove old request. rid={}, request={} ", requestId, botRequest);
                            oldRequests.add(requestId);
                        }
                    }

                    if (!oldRequests.isEmpty()) {
                        getLogger().warn("processMessage: Removed old requests={}", oldRequests.size());
                        requests.keySet().removeAll(oldRequests);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void updateRequestStatistics(IBotRequest handler, ITransportObject response) {

        String additionalInfo = "" + getId() + ":" + response.getRid() + " " +
                dtf.format(LocalDateTime.now());

        long elapsedTime = handler.getElapsedTime();

        StatisticsManager.getInstance().updateRequestStatistics(response.getClassName(),
                elapsedTime, additionalInfo);

        if (elapsedTime < 50) {
            StatisticsManager.getInstance().updateRequestStatistics(response.getClassName() + "[1] <50ms",
                    elapsedTime, additionalInfo);
        } else if (elapsedTime > 100 && elapsedTime < 500) {
            StatisticsManager.getInstance().updateRequestStatistics(response.getClassName() +
                    "[2] 100ms-500ms", elapsedTime, additionalInfo);
        } else if (elapsedTime > 500 && elapsedTime < 1000) {
            StatisticsManager.getInstance().updateRequestStatistics(response.getClassName() +
                    "[3] 500ms-1000ms", elapsedTime, additionalInfo);
        } else if (elapsedTime > 1000 && elapsedTime < 5000) {
            StatisticsManager.getInstance().updateRequestStatistics(response.getClassName() + "[4] 1s-5s",
                    elapsedTime, additionalInfo);
        } else if (elapsedTime > 5000 && elapsedTime < 10000) {
            StatisticsManager.getInstance().updateRequestStatistics(response.getClassName() + "[5] 5s-10s",
                    elapsedTime, additionalInfo);
        } else if (elapsedTime > 10000 && elapsedTime < 20000) {
            StatisticsManager.getInstance().updateRequestStatistics(response.getClassName() + "[6] 10s-20s",
                    elapsedTime, additionalInfo);
        } else if (elapsedTime > 20000 && elapsedTime < 30000) {
            StatisticsManager.getInstance().updateRequestStatistics(response.getClassName() + "[7] 20s-30s",
                    elapsedTime, additionalInfo);
        } else if (elapsedTime > 30000) {
            StatisticsManager.getInstance().updateRequestStatistics(response.getClassName() + "[8] >30s",
                    elapsedTime, additionalInfo);
        }
    }

    protected void logResponse(TObject response) {
        getLogger().debug("logResponse: Response={}", response);
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean isWssUrl() {
        return url.startsWith("wss:");
    }

    @Override
    public int getServerId() {
        return serverId;
    }

    @Override
    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    @Override
    public int getBankId() {
        return bankId;
    }

    @Override
    public void setBankId(int bankId) {
        this.bankId = bankId;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public Logger getLogger() {
        return LOG;
    }
}
