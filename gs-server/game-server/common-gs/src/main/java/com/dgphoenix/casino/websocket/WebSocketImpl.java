package com.abs.casino.websocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.*;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by vladislav on 11/16/16.
 */
@WebSocket
public class WebSocketImpl {
    private static final Logger LOG = LogManager.getLogger(WebSocketImpl.class);

    private static volatile WebSocketSessionsController sessionsController;

    public static void registerSessionsController(WebSocketSessionsController sessionsController) {
        WebSocketImpl.sessionsController = sessionsController;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        try {
            SessionWrapper sessionWrapper = SessionWrapper.of(session);
            String sessionId = sessionWrapper.getPlayerSessionId();
            checkNotNull(sessionId, "sessionId can't be null on connect");
            LOG.debug("onConnect: sessionId = {}", sessionId);

            sessionsController.openSession(sessionWrapper, sessionId);
        } catch (Exception e) {
            session.close(StatusCode.SERVER_ERROR, "Error during open connection");
            LOG.error("onConnect:: error, session is closed : {}", session, e);
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            SessionWrapper sessionWrapper = SessionWrapper.of(session);
            String sessionId = sessionWrapper.getPlayerSessionId();
            if (isTrimmedEmpty(sessionId)) {
                LOG.error("SessionId is null for session = {}", session);
                return;
            }

            checkNotNull(message, "Message can't be null");
            LOG.debug("OnMessage: sessionId = {}, message = {}", sessionId, message);

            sessionsController.processMessage(sessionId, message);
        } catch (Exception e) {
            session.close(StatusCode.SERVER_ERROR, "Error during process message");
            LOG.error("onMessage:: error, message : {},  session is closed : {}", message, session, e);
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int status, String reason) {
        try {
            SessionWrapper sessionWrapper = SessionWrapper.of(session);
            String sessionId = sessionWrapper.getPlayerSessionId();
            if (sessionId != null) {
                sessionsController.closeSession(sessionWrapper, sessionId, status, reason);
            } else {
                LOG.error("SessionId is null for session = {}", session);
            }
        } catch (Exception e) {
            LOG.error("onClose:: error, session : {}", session, e);
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        LOG.error("OnError: error occurred", cause);
    }
}
