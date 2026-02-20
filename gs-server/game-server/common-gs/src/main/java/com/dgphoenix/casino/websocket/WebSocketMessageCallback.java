package com.dgphoenix.casino.websocket;

import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;

/**
 * Created by vladislav on 12/29/16.
 */
public class WebSocketMessageCallback implements WriteCallback {
    private static final Logger LOG = LogManager.getLogger(WebSocketMessageCallback.class);
    private static final int MAX_ATTEMPTS_COUNT = 3;

    private final Session webSocketSession;
    private final String sessionId;
    private final String message;
    private final long startTime;
    private volatile byte attemptsCount = 1;

    public WebSocketMessageCallback(Session webSocketSession, String sessionId, String message) {
        this.webSocketSession = webSocketSession;
        this.sessionId = sessionId;
        this.message = message;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void writeFailed(Throwable x) {
        long sendingTime = System.currentTimeMillis() - startTime;
        LOG.error("Error during sending message = {}, sessionId = {}, webSocketSession = {}, attemptsCount = {}, " +
                "sendingTime = {}", message, sessionId, webSocketSession, attemptsCount, sendingTime, x);
        StatisticsManager.getInstance()
                .updateRequestStatistics(getClass().getSimpleName() + ":: writeFailed", sendingTime);

        if (!webSocketSession.isOpen()) {
            LOG.info("Session is already offline, stop retrying to send message");
            StatisticsManager.getInstance()
                    .updateRequestStatistics(getClass().getSimpleName() + ":: failedBecauseSessionIsClosed", sendingTime);

        } else if (attemptsCount >= MAX_ATTEMPTS_COUNT) {
            LOG.error("Stop retrying, max attempts done, attemptsCount = {}", attemptsCount);
            StatisticsManager.getInstance()
                    .updateRequestStatistics(getClass().getSimpleName() + ":: failedBecauseAttemptsCount", sendingTime);

        } else {
            attemptsCount++;
            webSocketSession.getRemote().sendString(message, this);
        }
    }

    @Override
    public void writeSuccess() {
        long sendingTime = System.currentTimeMillis() - startTime;
        LOG.info("Succeed sent message, sessionId = {}, message = {}, sendingTime = {}", sessionId, message, sendingTime);
        StatisticsManager.getInstance()
                .updateRequestStatistics(getClass().getSimpleName() + ":: writeSuccess", sendingTime);
    }
}
