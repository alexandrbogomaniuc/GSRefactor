package com.betsoft.casino.mp.web.socket;

import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.Latency;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.*;
import com.betsoft.casino.mp.web.handlers.IMessageHandler;
import com.betsoft.casino.utils.AESEncryptionDecryption;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TInboundObject;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.util.string.StringUtils;
import io.netty.buffer.ByteBufAllocator;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: flsh
 * Date: 03.11.17.
 */
public abstract class AbstractWebSocketHandler<T extends ISocketClient> implements IConfigurableWebSocketHandler {
    private static final long PING_INTERVAL = 20;
    private static final long DEFAULT_LATENCY_INTERVAL_SEC = 30;
    private static final long DEFAULT_LATENCY_GAME_SESSION_TRESHOLD_MS = 200;
    private static final double DEFAULT_LATENCY_THRESHOLD_MS = 100;
    private final Map<Class<? extends TObject>, IMessageHandler> handlers;
    private DataBufferFactory dataBufferFactory;
    private boolean localDevOriginsAllowed;

    @Value("${websocket.latency.interval.sec}")
    protected long latencyIntervalSec;
    @Value("${websocket.latency.game.session.threshold.ms}")
    protected long latencyGameSessionThresholdMs;
    @Value("${websocket.latency.ping.type.enabled}")
    protected boolean latencyPingTypeEnabled;
    @Value("${websocket.latency.standard.type.enabled}")
    protected boolean latencyStandardTypeEnabled;
    @Value("${websocket.latency.threshold.ms}")
    protected double latencyThresholdMS;
    protected ConcurrentMap<String, Disposable> latencyDisposables = new ConcurrentHashMap<>();

    public AbstractWebSocketHandler() {
        handlers = new HashMap<>();
        dataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        localDevOriginsAllowed = false;
        latencyIntervalSec = 2;
        if (latencyIntervalSec == 0) {
            latencyIntervalSec = DEFAULT_LATENCY_INTERVAL_SEC;
        }
        if (latencyGameSessionThresholdMs == 0) {
            latencyGameSessionThresholdMs = DEFAULT_LATENCY_GAME_SESSION_TRESHOLD_MS;
        }
        if (latencyThresholdMS == 0) {
            latencyThresholdMS = DEFAULT_LATENCY_THRESHOLD_MS;
        }
    }

    abstract void createConnection(WebSocketSession session, FluxSink<WebSocketMessage> sink);

    protected void startPing(WebSocketSession session, FluxSink<WebSocketMessage> sink) {
        Flux.interval(Duration.ofSeconds(PING_INTERVAL))
                .takeUntil(i -> !isConnected(session))
                .map(i -> session.pingMessage(factory -> factory.wrap(AESEncryptionDecryption.encryptTimestamp(System.currentTimeMillis()))))
                .doOnNext(sink::next)
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }

    protected Disposable startLatencyMeasurement(WebSocketSession session) {
        T client = getClient(session);
        // Store the subscription so that it can be disposed of later
        return Flux.interval(Duration.ofSeconds(latencyIntervalSec))
                .doOnTerminate(() -> System.out.println("startLatencyMeasurement: Terminating latency measurement for session " + session.getId()))
                .subscribe(e ->
                                client.sendMessage(Latency.Builder.newBuilder(System.currentTimeMillis(), -1, 1)
                                        .withServerTs(System.currentTimeMillis())
                                        .build())

                        , error -> {
                            getLog().error("startLatencyMeasurement: Error during latency measurement for session {}. Error: {}", session.getId(), error.getMessage());
                        });

    }

    protected boolean isConnected(WebSocketSession session) {
        return false;
    }

    abstract void closeConnection(WebSocketSession session);

    abstract IMessageSerializer getSerializer();

    abstract Logger getLog();

    abstract T getClient(WebSocketSession session);

    protected TObject deserialize(WebSocketMessage message) {
        try {
            return getSerializer().deserialize(message);
        } catch (Exception e) {
            getLog().error("Bad request: {}", message, e);
        }
        return null;
    }

    public IMessageHandler getHandler(Class<? extends ITransportObject> klass) {
        return handlers.get(klass);
    }


    public boolean isAllowedOrigin(String origin) {
        URL url = null;
        try {
            url = new URL(origin);
        } catch (MalformedURLException e) {
            //allow if origin is wrong, because we allow also if origin == null || origin.isEmpty()
            return true;
        }

        String host = url.getHost();

        return isLocalDevOriginsAllowed()
                || origin == null || origin.isEmpty()
                || host.endsWith("primarygoose.com")
                || host.endsWith(".dgphoenix.com")
                || host.endsWith(".discreetgaming.com")
                || host.endsWith(".betsoftgaming.com")
                || host.endsWith(".nucleusgaming.com")
                || host.endsWith(".maxquest.com")
                || host.endsWith(".mydomain")
                || host.endsWith(".mqbase.com")
                || host.endsWith(".mdtest.io")
                || host.endsWith(".mdbase.io")
                || host.endsWith(".maxduel.com")
                || host.endsWith(".maxduel.io");
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        try {
            changeDefaultConfig(session, getLog());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            getLog().error("Reflective Operation Exception", e);
        }

        String origin = session.getHandshakeInfo().getHeaders().getFirst("Origin");
        if (!isAllowedOrigin(origin)) {

            getLog().error("originIsWrong exception", new Exception("wrong origin: '" + origin + "'"));
            return session.close(CloseStatus.POLICY_VIOLATION);

        }

        Mono<Void> outbound = session.send(
                Flux.create((FluxSink<WebSocketMessage> sink) ->
                                createConnection(session, sink), FluxSink.OverflowStrategy.BUFFER
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

    void processMessage(WebSocketSession session, WebSocketMessage message) {
        if (WebSocketMessage.Type.PONG.equals(message.getType())) {
            if (latencyPingTypeEnabled && (this instanceof GameWebSocketHandler || this instanceof  UnifiedWebSocketHandler)) {
                measurePingLatency(message, session);
            }
            return;
        }

        T client = getClient(session);
        if (client == null) {
            getLog().error("Cannot process, client not found, message={}", message.getPayloadAsText());
            if (isDropOnBadMessage()) {
                session.close(CloseStatus.SERVER_ERROR);
            }
            return;
        }

        TObject tMessage = deserialize(message);
        if (tMessage == null) {
            sendMessage(client, createErrorMessage(ErrorCodes.BAD_REQUEST, "Bad request: '" +
                    message.getPayloadAsText() + "'", -1));
            if (isDropOnBadMessage()) {
                getLog().error("Drop connection, cannot parse message");
                session.close(CloseStatus.BAD_DATA);
            }
            return;
        }

        getLog().debug("processMessage: {}, session.id={}", tMessage, session.getId());

        IMessageHandler handler = getHandler(tMessage.getClass());
        if (handler != null) {
            try {
                RequestStatistic requestStatistic = client.getRequestStatistic(tMessage.getClass());
                if (requestStatistic != null && !handler.isLimitApproved(tMessage, requestStatistic, tMessage.getFrequencyLimit())) {
                    sendMessage(client, createErrorMessage(ErrorCodes.REQUEST_FREQ_LIMIT_EXCEEDED, "Too many requests",
                            tMessage.getRid()));
                } else {

                    client.addRequestStatistic(tMessage.getClass(), tMessage.getRid(), System.currentTimeMillis(), tMessage.getDate());

                    handler.handle(session, tMessage, client);

                }
            } catch (Exception e) {
                getLog().error("Unexpected error", e);
                Error error = createErrorMessage(ErrorCodes.INTERNAL_ERROR, "Internal error", tMessage.getRid());
                if (tMessage instanceof TInboundObject) {
                    client.sendMessage(error);
                } else {
                    client.sendMessage(error, (TInboundObject) tMessage);
                }
            }

            onSuccess(tMessage, client);

        } else {
            getLog().info("Handler for message of type '{}' not found", tMessage.getClass());
            sendMessage(client, createErrorMessage(ErrorCodes.BAD_REQUEST, "Bad request", tMessage.getRid()));
        }
    }

    protected void measurePingLatency(WebSocketMessage message, WebSocketSession session) {
    }

    abstract void onSuccess(TObject message, T client);

    protected boolean isDropOnBadMessage() {
        //todo: must be configurable, true on production, false on dev/staging
        return false;
    }

    public void register(Class<? extends TObject> klass, IMessageHandler handler) {
        handlers.put(klass, handler);
    }

    Error createErrorMessage(int code, String msg, int rid) {
        return new Error(code, msg, System.currentTimeMillis(), rid);
    }

    void sendMessage(T client, TObject obj) {
        client.sendMessage(obj);
    }

    public boolean isLocalDevOriginsAllowed() {
        return localDevOriginsAllowed;
    }

    public void setLocalDevOriginsAllowed(boolean localDevOriginsAllowed) {
        this.localDevOriginsAllowed = localDevOriginsAllowed;
    }

    protected void registerLatencyDisposable(WebSocketSession session, Disposable latencyDisposable) {
        latencyDisposables.put(session.getId(), latencyDisposable);
    }

    protected void unregisterLatencyDisposable(WebSocketSession session) {
        Disposable disposable = latencyDisposables.get(session.getId());
        if (disposable == null) {
            return;
        }
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
        latencyDisposables.remove(session.getId());
    }

    protected void logSessionLatency(IGameSocketClient client) {
        String sessionStat = client.getLatencyStatistic().getName() + "; " +
                " count: " + client.getLatencyStatistic().getExperimentCount() + "; " +
                " avg: " + client.getLatencyStatistic().getAvgValue() + " ms;" +
                " min: " + client.getLatencyStatistic().getMinValue() + " ms;" +
                " max: " + client.getLatencyStatistic().getMaxValue() + " ms;" +
                " [" + (StringUtils.isTrimmedEmpty(client.getLatencyStatistic().getMaxValueInfo()) ? "" : client.getLatencyStatistic().getMaxValueInfo()) + "] " +
                " maxValueTime: " + (new Date(client.getLatencyStatistic().getMaxValueTime()));

        String sessionPingStat = client.getPingLatencyStatistic().getName() + "; " +
                " count: " + client.getPingLatencyStatistic().getExperimentCount() + "; " +
                " avg: " + client.getPingLatencyStatistic().getAvgValue() + " ms;" +
                " min: " + client.getPingLatencyStatistic().getMinValue() + " ms;" +
                " max: " + client.getPingLatencyStatistic().getMaxValue() + " ms;" +
                " [" + (StringUtils.isTrimmedEmpty(client.getPingLatencyStatistic().getMaxValueInfo()) ? "" : client.getPingLatencyStatistic().getMaxValueInfo()) + "] " +
                " maxValueTime: " + (new Date(client.getPingLatencyStatistic().getMaxValueTime()));

        if (latencyStandardTypeEnabled) {
            if (client.getLatencyStatistic().getAvgValue() > latencyGameSessionThresholdMs) {
                getLog().warn("closeConnection: HIGH LATENCY for {}", sessionStat);
            } else {
                getLog().info("closeConnection: LATENCY for {}", sessionStat);
            }
        }
        if (latencyPingTypeEnabled) {
            if (client.getPingLatencyStatistic().getAvgValue() > latencyGameSessionThresholdMs) {
                getLog().warn("closeConnection: HIGH PING LATENCY for {}", sessionPingStat);
            } else {
                getLog().info("closeConnection: LOW PING LATENCY for {}", sessionPingStat);
            }
        }
    }
}
