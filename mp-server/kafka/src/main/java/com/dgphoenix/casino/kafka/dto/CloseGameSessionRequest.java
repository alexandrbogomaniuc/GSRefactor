package com.dgphoenix.casino.kafka.dto;

public class CloseGameSessionRequest implements KafkaRequest {
    private String sessionId;
    private long accountId;
    private long gameSessionId;
    private long buyIn;

    public CloseGameSessionRequest() {}

    public CloseGameSessionRequest(String sessionId,
            long accountId,
            long gameSessionId,
            long buyIn) {
        this.sessionId = sessionId;
        this.accountId = accountId;
        this.gameSessionId = gameSessionId;
        this.buyIn = buyIn;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public long getBuyIn() {
        return buyIn;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public void setBuyIn(long buyIn) {
        this.buyIn = buyIn;
    }
}
