package com.dgphoenix.casino.websocket.tournaments;

import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@WebSocket
public class TournamentWebSocket {
    private static final Logger LOG = LogManager.getLogger(TournamentWebSocket.class);

    private static TournamentWebSocketSessionsController sessionsController;
    private static final Gson GSON = GsonFactory.createGson();

    public static void registerSessionsController(TournamentWebSocketSessionsController sessionsController) {
        TournamentWebSocket.sessionsController = sessionsController;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        try {
            TournamentClient client = new TournamentClient(session, getSessionId(session), GSON);
            LOG.debug("Established websocket connection");
            sessionsController.openSession(client);
        } catch (Exception e) {
            session.close(StatusCode.SERVER_ERROR, "Error during open connection");
            LOG.error("onConnect:: error, session is closed : {}", session, e);
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            String sessionId = getSessionId(session);
            checkNotNull(sessionId, "Session id is missed");
            checkNotNull(message, "Message can't be null");
            LOG.debug("OnMessage: sessionId={}, message={}", sessionId, message);

            sessionsController.processMessage(sessionId, message);
        } catch (Exception e) {
            session.close(StatusCode.SERVER_ERROR, "Error during process message");
            LOG.error("onMessage:: error, message: {},  session is closed: {}", message, session, e);
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int status, String reason) {
        try {
            String sessionId = getSessionId(session);
            if (sessionId != null) {
                sessionsController.closeSession(sessionId, status, reason);
            } else {
                LOG.error("sessionId is null for session={}", session);
                if (session.isOpen()) {
                    session.close();
                }
            }
        } catch (Exception e) {
            LOG.error("onClose:: error, session: {}", session, e);
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        LOG.error("OnError: error occurred", cause);
    }

    private String getSessionId(Session session) {
        Map<String, List<String>> parametersMap = session.getUpgradeRequest().getParameterMap();
        List<String> values = parametersMap.get("sessionId");
        String value = null;
        if (CollectionUtils.isNotEmpty(values)) {
            value = values.get(0);
        }
        return value;
    }
}
