package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class GetPaymentOperationStatus2Request implements KafkaRequest {
    private long accountId;
    private long roomId;
    private long roundId;
    private String sessionId;
    private long gameSessionId;
    private long gameId;
    private long bankId;
    private Boolean isBet;
    private int betNumber;

    public GetPaymentOperationStatus2Request() {}

    public GetPaymentOperationStatus2Request(long accountId,
            long roomId,
            long roundId,
            String sessionId,
            long gameSessionId,
            long gameId,
            long bankId,
            Boolean isBet,
            int betNumber) {
        super();
        this.accountId = accountId;
        this.roomId = roomId;
        this.roundId = roundId;
        this.sessionId = sessionId;
        this.gameSessionId = gameSessionId;
        this.gameId = gameId;
        this.bankId = bankId;
        this.isBet = isBet;
        this.betNumber = betNumber;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getRoomId() {
        return roomId;
    }

    public long getRoundId() {
        return roundId;
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

    public long getBankId() {
        return bankId;
    }

    public Boolean getIsBet() {
        return isBet;
    }

    public int getBetNumber() {
        return betNumber;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
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

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public void setIsBet(Boolean isBet) {
        this.isBet = isBet;
    }

    public void setBetNumber(int betNumber) {
        this.betNumber = betNumber;
    }
}
