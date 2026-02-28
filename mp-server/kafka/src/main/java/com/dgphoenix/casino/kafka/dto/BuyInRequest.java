package com.abs.casino.kafka.dto;

public class BuyInRequest implements KafkaRequest {
    private String sessionId;
    private long cents;
    private long gameSessionId;
    private long roomId;
    private int betNumber;
    private long tournamentId;
    private long currentBalance;
    private long roundId;

    public BuyInRequest() {}

    public BuyInRequest(String sessionId,
            long cents,
            long gameSessionId,
            long roomId,
            int betNumber,
            long tournamentId,
            long currentBalance,
            long roundId) {
        this.sessionId = sessionId;
        this.cents = cents;
        this.gameSessionId = gameSessionId;
        this.roomId = roomId;
        this.betNumber = betNumber;
        this.tournamentId = tournamentId;
        this.currentBalance = currentBalance;
        this.roundId = roundId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getCents() {
        return cents;
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

    public long getTournamentId() {
        return tournamentId;
    }

    public long getCurrentBalance() {
        return currentBalance;
    }

    public long getRoundId() {
        return roundId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setCents(long cents) {
        this.cents = cents;
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

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public void setCurrentBalance(long currentBalance) {
        this.currentBalance = currentBalance;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }
}
