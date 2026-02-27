package com.abs.casino.common.mp;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardResult {
    private long leaderboardId;
    private long transactionId;
    private long startDate;
    private long endDate;
    private long gameId;
    private String currency;
    private List<LeaderboardWinner> winners;

    public LeaderboardResult(long leaderboardId, long startDate, long endDate, long gameId, String currency) {
        this.leaderboardId = leaderboardId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.gameId = gameId;
        this.currency = currency;
        this.winners = new ArrayList<LeaderboardWinner>();
    }

    public LeaderboardResult(long leaderboardId, long startDate, long endDate, long gameId, String currency, List<LeaderboardWinner> winners) {
        this.leaderboardId = leaderboardId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.gameId = gameId;
        this.currency = currency;
        this.winners = winners;
    }

    public long getLeaderboardId() {
        return leaderboardId;
    }

    public long getStartDate() {
        return startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public long getGameId() {
        return gameId;
    }

    public String getCurrency() {
        return currency;
    }

    public List<LeaderboardWinner> getWinners() {
        return winners;
    }

    public void addWinner(LeaderboardWinner winner) {
        winners.add(winner);
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        return "LeaderboardResult{" +
                "leaderboardId=" + leaderboardId +
                ", transactionId=" + transactionId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", gameId=" + gameId +
                ", currency='" + currency + '\'' +
                ", winners=" + winners +
                '}';
    }
}
