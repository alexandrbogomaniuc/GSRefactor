package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.KafkaRequest;

public class RefundBuyInRequest implements KafkaRequest {
    private String sessionId;
    private long cents;
    private long accountId;
    private long gameSessionId;
    private long roomId;
    private int betNumber;

    public RefundBuyInRequest() {}

    public RefundBuyInRequest(String sessionId,
            long cents,
            long accountId,
            long gameSessionId,
            long roomId,
            int betNumber) {
        super();
        this.sessionId = sessionId;
        this.cents = cents;
        this.accountId = accountId;
        this.gameSessionId = gameSessionId;
        this.roomId = roomId;
        this.betNumber = betNumber;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getCents() {
        return cents;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public long getRoomId() {
        return roomId;
    }

    public int getBetNumber() {
        return betNumber;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setCents(long cents) {
        this.cents = cents;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public void setBetNumber(int betNumber) {
        this.betNumber = betNumber;
    }
}
