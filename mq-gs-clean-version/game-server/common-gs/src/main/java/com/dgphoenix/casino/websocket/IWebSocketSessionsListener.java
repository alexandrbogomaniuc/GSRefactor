package com.dgphoenix.casino.websocket;

/**
 * Created by vladislav on 1/12/17.
 */
public interface IWebSocketSessionsListener {
    void notifyWebSocketForSessionIsOpen(String sessionId, SessionWrapper webSocketSession);

    void notifyWebSocketForSessionIsClosed(String sessionId);
}
