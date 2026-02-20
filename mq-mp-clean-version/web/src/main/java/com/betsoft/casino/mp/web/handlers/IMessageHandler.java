package com.betsoft.casino.mp.web.handlers;

import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.RoomMoved;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.mp.web.RequestStatistic;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TInboundObject;
import com.betsoft.casino.utils.TObject;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.net.InetSocketAddress;

/**
 * User: flsh
 * Date: 03.11.17.
 */
public interface IMessageHandler<MESSAGE extends ITransportObject, CLIENT extends ISocketClient> {
    long MAX_ALLOWED_CLIENT_DRIFT = 30000;

    void handle(WebSocketSession session, MESSAGE message, CLIENT client);

    default long getCurrentTime() {
        return System.currentTimeMillis();
    }

    default Error createErrorMessage(int code, String msg, int rid) {
        return new Error(code, msg, getCurrentTime(), rid);
    }

    default void sendErrorMessage(CLIENT client, int code, String msg, int rid) {
        Error errorMessage = createErrorMessage(code, msg, rid);
        getLog().error("error, message=  {}, client={}", errorMessage, client);
        client.sendMessage(errorMessage);
    }

    default void sendErrorMessage(CLIENT client, int code, String msg, int rid, TInboundObject inboundObject) {
        Error errorMessage = createErrorMessage(code, msg, rid);
        getLog().error("error, message=  {}, client={}", errorMessage, client);
        client.sendMessage(errorMessage, inboundObject);
    }

    default void sendErrorMessage(CLIENT client, int code, int rid, TInboundObject inboundObject) {
        sendErrorMessage(client, code, "", rid, inboundObject);
    }

    default void sendRoomMovedMessage(CLIENT client, int rid, int newServerId, long roomId, String newStartGameUrl) {
        RoomMoved message = new RoomMoved(getCurrentTime(), rid, roomId, newServerId, newStartGameUrl);
        client.sendMessage(message);
    }

    default void sendDeprecatedError(CLIENT client, int rid) {
        Error errorMessage = createErrorMessage(ErrorCodes.DEPRECATED_REQUEST, "Deprecated", rid);
        getLog().error("error, message=  {}, client={}", errorMessage, client);
        client.sendMessage(errorMessage);
    }

    default void processRebootError(CLIENT client, MESSAGE message, Exception e) {
        Error errorMessage = createErrorMessage(ErrorCodes.MQ_SERVER_REBOOT, "Reboot error: " + e.getMessage(),
                message.getRid());
        getLog().error("Reboot error, message={}, client={}", errorMessage, client);
        getLog().error("Stacktrace: ", e);
        client.sendMessage(errorMessage);
    }


    default void processUnexpectedError(CLIENT client, MESSAGE message, Exception e) {
        Error errorMessage = createErrorMessage(ErrorCodes.INTERNAL_ERROR, "Internal error: " + e.getMessage(),
                message.getRid());
        getLog().error("Unexpected error, message={}, client={}", errorMessage, client);
        getLog().error("Stacktrace: ", e);
        client.sendMessage(errorMessage);
    }

    static String getOrigin(WebSocketSession session) {
        String origin = session.getHandshakeInfo().getHeaders().getFirst("Origin");
        if (origin == null) {
            return "http://default-gp3.local.com";
        }
        return origin;
    }

    default String getHost(WebSocketSession session) {
        InetSocketAddress host = session.getHandshakeInfo().getHeaders().getHost();
        if (host == null) {
            return "localhost:8080";
        }
        return host.getPort() == 0 ? host.getHostName() : host.toString();
    }

    static String getWsProtocol(String origin) {
        return origin.startsWith("https://") ? "wss://" : "ws://";
    }

    default boolean isLimitApproved(TObject tMessage, RequestStatistic requestStatistic, int limit) {

        long clientDelta = tMessage.getDate() - requestStatistic.getLastClientSendDate();
        if(clientDelta < limit ) {
            getLog().debug("isLimitApproved: return false, clientDelta < limit is true, clientDelta={}, limit={}", clientDelta, limit);
            return false;
        }

        long now = System.currentTimeMillis();
        long serverDelta = now - requestStatistic.getLastServerInputDate();
        long serverClientDelta = Math.abs(requestStatistic.getServerClientDelta());
        long messageDelta = Math.abs(now - tMessage.getDate());
        long clientDrift = messageDelta - serverClientDelta;

        getLog().debug("isLimitApproved: now={}, LastServerInputDate={}, serverDelta={}, serverClientDelta={}, " +
                        "messageDate={}, messageDelta={}, clientDrift={}",
                now, requestStatistic.getLastServerInputDate(), serverDelta, serverClientDelta, tMessage.getDate(),
                messageDelta, clientDrift);

        if(serverDelta < limit && clientDrift > MAX_ALLOWED_CLIENT_DRIFT) {
            getLog().debug("isLimitApproved: return false, maxAllowedClientDrift found " +
                    "(serverDelta < limit && clientDrift > MAX_ALLOWED_CLIENT_DRIFT) is true");
            return false;
        }

        getLog().debug("isLimitApproved: return true");
        return true;
    }

    Logger getLog();
}
