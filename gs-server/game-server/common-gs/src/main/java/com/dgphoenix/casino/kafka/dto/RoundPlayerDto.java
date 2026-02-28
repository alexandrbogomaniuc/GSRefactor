package com.abs.casino.kafka.dto;

public class RoundPlayerDto {
    private long accountId;
    private String sessionId;
    private long gameSessionId;

    public RoundPlayerDto() {}

    public RoundPlayerDto(long accountId, String sessionId, long gameSessionId) {
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.gameSessionId = gameSessionId;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }
}
