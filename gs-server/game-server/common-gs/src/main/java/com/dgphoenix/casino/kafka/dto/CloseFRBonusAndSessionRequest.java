package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class CloseFRBonusAndSessionRequest implements KafkaRequest {
    private long accountId;
    private String sessionId;
    private long gameSessionId;
    private long gameId;
    private long bonusId;
    private long winSum;

    public CloseFRBonusAndSessionRequest() {}

    public CloseFRBonusAndSessionRequest(long accountId,
            String sessionId,
            long gameSessionId,
            long gameId,
            long bonusId,
            long winSum) {
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.gameSessionId = gameSessionId;
        this.gameId = gameId;
        this.bonusId = bonusId;
        this.winSum = winSum;
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

    public long getGameId() {
        return gameId;
    }

    public long getBonusId() {
        return bonusId;
    }

    public long getWinSum() {
        return winSum;
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

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public void setBonusId(long bonusId) {
        this.bonusId = bonusId;
    }

    public void setWinSum(long winSum) {
        this.winSum = winSum;
    }
}
