package com.abs.casino.common.exception;

import com.dgphoenix.casino.common.exception.CommonException;

public class CannotCloseMultiplayerGameException extends CommonException {
    private String sessionId;
    private long gameSessionId;

    public CannotCloseMultiplayerGameException(String message, String sessionId, long gameSessionId) {
        super(message);
        this.sessionId = sessionId;
        this.gameSessionId = gameSessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }
}
