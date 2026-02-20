package com.betsoft.casino.bots;

import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.mp.web.RequestStatistic;
import com.betsoft.casino.utils.ITransportObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;

import java.util.Collection;
import java.util.Collections;

public class ProxyClient implements ISocketClient {
    private static final Logger LOG = LogManager.getLogger(ProxyClient.class);

    private final ISender sender;

    public ProxyClient(ISender sender) {
        this.sender = sender;
    }

    @Override
    public FluxSink<WebSocketMessage> sendMessage(ITransportObject message) {
        sender.send(message);
        return null;
    }

    @Override
    public FluxSink<WebSocketMessage> sendMessage(WebSocketMessage message) {
        LOG.error("Unexpected message: " + message.getPayloadAsText());
        return null;
    }

    @Override
    public boolean isBot() {
        return true;
    }

    @Override
    public FluxSink<WebSocketMessage> getConnection() {
        return null;
    }

    @Override
    public IMessageSerializer getSerializer() {
        return null;
    }

    @Override
    public String getWebSocketSessionId() {
        return null;
    }

    @Override
    public WebSocketSession getSession() {
        return null;
    }

    @Override
    public Long getAccountId() {
        return null;
    }

    @Override
    public Long getBankId() {
        return 271L;
    }

    @Override
    public int getRid() {
        return 0;
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
    public void addRequestStatistic(Class requestClass, long lastRequestId, long lastServerInputDate, long lastClientSendDate) {
        //nop
    }

    @Override
    public RequestStatistic getRequestStatistic(Class requestClass) {
        return null;
    }

    @Override
    public Collection<RequestStatistic> getRequestStatistics() {
        return Collections.emptySet();
    }

    @Override
    public boolean isSingleConnectionClient() {
        return false;
    }
}
