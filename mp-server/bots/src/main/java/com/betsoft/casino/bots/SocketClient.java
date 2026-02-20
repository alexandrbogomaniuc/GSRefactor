package com.betsoft.casino.bots;

import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.mp.web.RequestStatistic;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;

import java.util.Collection;

public class SocketClient implements ISocketClient {

    private final WebSocketSession session;
    private final FluxSink<WebSocketMessage> connection;
    private final IMessageSerializer serializer;
    private final Logger LOG;
    private final int bankId;

    public SocketClient(WebSocketSession session, FluxSink<WebSocketMessage> connection, IMessageSerializer serializer,
                        int bankId, Logger log) {
        this.session = session;
        this.connection = connection;
        this.serializer = serializer;
        this.bankId = bankId;
        this.LOG = log;
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
    public String getWebSocketSessionId() {
        return session.getId();
    }

    @Override
    public Long getAccountId() {
        return null;
    }

    @Override
    public Long getBankId() {
        return (long) bankId;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public boolean isBot() {
        return true;
    }

    @Override
    public int getRid() {
        return RequestIdGenerator.getInstance().next();
    }

    @Override
    public int getServerId() {
        return 1;
    }

    @Override
    public void setServerId(int serverId) {
        //nop
    }

    @Override
    public String getSessionId() {
        return null;
    }

    @Override
    public void setSessionId(String sessionId) {
        //nop
    }

    @Override
    public WebSocketSession getSession() {
        return session;
    }

    @Override
    public void addRequestStatistic(Class requestClass, long lastRequestId, long lastServerInputDate,
                                    long lastClientSendDate) {

    }

    @Override
    public RequestStatistic getRequestStatistic(Class requestClass) {
        return null;
    }

    @Override
    public Collection<RequestStatistic> getRequestStatistics() {
        return null;
    }

    @Override
    public boolean isSingleConnectionClient() {
        return false;
    }
}
