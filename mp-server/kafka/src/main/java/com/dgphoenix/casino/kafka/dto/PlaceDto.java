package com.abs.casino.kafka.dto;

public class PlaceDto {
    private long accountId;
    private long win;
    private int rank;
    private long betsSum;
    private long winSum;
    private long gameSessionId;
    private long gameScore;
    private double ejectPoint;

    public PlaceDto() {}

    public PlaceDto(long accountId,
            long win,
            int rank,
            long betsSum,
            long winSum,
            long gameSessionId,
            long gameScore,
            double ejectPoint) {
        super();
        this.accountId = accountId;
        this.win = win;
        this.rank = rank;
        this.betsSum = betsSum;
        this.winSum = winSum;
        this.gameSessionId = gameSessionId;
        this.gameScore = gameScore;
        this.ejectPoint = ejectPoint;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getWin() {
        return win;
    }

    public int getRank() {
        return rank;
    }

    public long getBetsSum() {
        return betsSum;
    }

    public long getWinSum() {
        return winSum;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public long getGameScore() {
        return gameScore;
    }

    public double getEjectPoint() {
        return ejectPoint;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setWin(long win) {
        this.win = win;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setBetsSum(long betsSum) {
        this.betsSum = betsSum;
    }

    public void setWinSum(long winSum) {
        this.winSum = winSum;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public void setGameScore(long gameScore) {
        this.gameScore = gameScore;
    }

    public void setEjectPoint(double ejectPoint) {
        this.ejectPoint = ejectPoint;
    }
}
