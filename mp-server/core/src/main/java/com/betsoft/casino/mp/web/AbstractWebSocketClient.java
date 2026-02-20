package com.betsoft.casino.mp.web;

import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.FluxSink;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * User: flsh
 * Date: 05.10.18.
 */
public abstract class AbstractWebSocketClient implements ISocketClient {
    private Map<Class, RequestStatistic> requestStatisticMap = new HashMap<>();
    private final String webSocketSessionId;
    protected FluxSink<WebSocketMessage> connection;
    protected final IMessageSerializer serializer;

    public AbstractWebSocketClient(String webSocketSessionId, FluxSink<WebSocketMessage> connection,
                                   IMessageSerializer serializer) {
        this.webSocketSessionId = webSocketSessionId;
        this.connection = connection;
        this.serializer = serializer;
    }

    @Override
    public String getWebSocketSessionId() {
        return webSocketSessionId;
    }

    @Override
    public FluxSink<WebSocketMessage> getConnection() {
        return connection;
    }

    @Override
    public IMessageSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void addRequestStatistic(Class requestClass,
                                    long lastRequestId, long lastServerInputDate, long lastClientSendDate) {
        RequestStatistic statistic = getRequestStatistic(requestClass);
        if(statistic == null) {
            long delta = Math.abs(System.currentTimeMillis() - lastClientSendDate);
            statistic = new RequestStatistic(requestClass, lastRequestId, delta,
                    lastServerInputDate, lastClientSendDate);
            requestStatisticMap.put(requestClass, statistic);
        } else {
            statistic.doNext(lastRequestId, lastServerInputDate, lastClientSendDate);
        }
    }

    @Override
    public RequestStatistic getRequestStatistic(Class requestClass) {
        return requestStatisticMap.get(requestClass);
    }

    @Override
    public Collection<RequestStatistic> getRequestStatistics() {
        return requestStatisticMap.values();
    }

    @Override
    public boolean isSingleConnectionClient() {
        return false;
    }
}
