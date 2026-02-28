package com.abs.casino.kafka.dto;

import java.util.Map;

public class SitOutRequest implements KafkaRequest {
    private String sessionId;
    private long gameSessionId;
    private long cents;
    private long returnedBet;
    private long roundId;
    private long roomId;
    private long accountId;
    private RoundInfoResultDto roundInfo;
    private Map<Long, Double> contributions;

    public SitOutRequest() {}

    public SitOutRequest(String sessionId,
            long gameSessionId,
            long cents,
            long returnedBet,
            long roundId,
            long roomId,
            long accountId,
            RoundInfoResultDto roundInfo,
            Map<Long, Double> contributions) {
        super();
        this.sessionId = sessionId;
        this.gameSessionId = gameSessionId;
        this.cents = cents;
        this.returnedBet = returnedBet;
        this.roundId = roundId;
        this.roomId = roomId;
        this.accountId = accountId;
        this.roundInfo = roundInfo;
        this.contributions = contributions;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public long getCents() {
        return cents;
    }

    public long getReturnedBet() {
        return returnedBet;
    }

    public long getRoundId() {
        return roundId;
    }

    public long getRoomId() {
        return roomId;
    }

    public long getAccountId() {
        return accountId;
    }

    public RoundInfoResultDto getRoundInfo() {
        return roundInfo;
    }

    public Map<Long, Double> getContributions() {
        return contributions;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public void setCents(long cents) {
        this.cents = cents;
    }

    public void setReturnedBet(long returnedBet) {
        this.returnedBet = returnedBet;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setRoundInfo(RoundInfoResultDto roundInfo) {
        this.roundInfo = roundInfo;
    }

    public void setContributions(Map<Long, Double> contributions) {
        this.contributions = contributions;
    }
}
