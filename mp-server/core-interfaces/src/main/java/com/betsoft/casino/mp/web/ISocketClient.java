package com.betsoft.casino.mp.web;

import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.InboundObject;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 03.11.17.
 */
public interface ISocketClient {
    Logger LOG = LogManager.getLogger(ISocketClient.class);

    FluxSink<WebSocketMessage> getConnection();

    IMessageSerializer getSerializer();

    String getWebSocketSessionId();

    WebSocketSession getSession();

    // TODO: implement in subclasses and remove default
    default boolean isLoggedIn() {
        return true;
    }

    Long getAccountId();

    Long getBankId();

    int getRid();

    default boolean isDisconnected() {
        return false;
    }

    int getServerId();

    void setServerId(int serverId);

    default void setLastUpdatedBalance(Long lastUpdatedBalance) {
        //nop
    }

    String getSessionId();

    void setSessionId(String sessionId);

    void addRequestStatistic(Class requestClass, long lastRequestId, long lastServerInputDate, long lastClientSendDate);

    RequestStatistic getRequestStatistic(Class requestClass);

    Collection<RequestStatistic> getRequestStatistics();

    default Logger getLog() {
        return LOG;
    }

    default FluxSink<WebSocketMessage> sendMessage(ITransportObject outbound) {
        WebSocketMessage message = getSerializer().serialize(outbound);
        return sendMessage(message);
    }

    default FluxSink<WebSocketMessage> sendMessage(ITransportObject outbound, InboundObject inbound) {
        long time = System.currentTimeMillis();
        String requestName = outbound.getClassName().contains("Error") ?
                inbound.getClassName() + "[error]" : inbound.getClassName();
        if (inbound.getInboundDate() <= 0) {
            getLog().warn("sendMessage: cannot update statistics, inbound date not set, inbound={}", inbound);
        } else {
            StatisticsManager.getInstance().updateRequestStatistics(requestName, time - inbound.getInboundDate(),
                    getSessionId() + ":" + inbound.getRid());
        }
        WebSocketMessage message = getSerializer().serialize(outbound);
        long clientTime = time - inbound.getDate();
        if (clientTime > 0 && clientTime < TimeUnit.SECONDS.toMillis(60)) {
            StatisticsManager.getInstance().updateRequestStatistics(requestName + "[client]", clientTime,
                    getSessionId() + ":" + inbound.getRid());
        }
        return sendMessage(message);
    }

    default FluxSink<WebSocketMessage> sendMessage(WebSocketMessage message) {
        //don't use message.getPayloadAsText() for logging!!! readPosition cannot be reset
        getLog().debug("{}, accountId: {} :: Sending message: {}", getWebSocketSessionId(), getAccountId(),  message);
        return getConnection().next(message);
    }

    boolean isBot();

    boolean isSingleConnectionClient();
}
