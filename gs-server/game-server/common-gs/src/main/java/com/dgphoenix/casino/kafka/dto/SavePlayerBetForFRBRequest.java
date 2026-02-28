package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.RoundInfoResultDto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class SavePlayerBetForFRBRequest implements KafkaRequest {
    private String sessionId;
    private long gameSessionId;
    private long roundId;
    private long accountId;
    private RoundInfoResultDto roundInfo;

    public SavePlayerBetForFRBRequest() {}

    public SavePlayerBetForFRBRequest(String sessionId,
            long gameSessionId,
            long roundId,
            long accountId,
            RoundInfoResultDto roundInfo) {
        this.sessionId = sessionId;
        this.gameSessionId = gameSessionId;
        this.roundId = roundId;
        this.accountId = accountId;
        this.roundInfo = roundInfo;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public long getRoundId() {
        return roundId;
    }

    public long getAccountId() {
        return accountId;
    }

    public RoundInfoResultDto getRoundInfo() {
        return roundInfo;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setRoundInfo(RoundInfoResultDto roundInfo) {
        this.roundInfo = roundInfo;
    }
}
