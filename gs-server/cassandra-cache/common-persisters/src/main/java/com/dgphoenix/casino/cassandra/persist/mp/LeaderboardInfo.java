package com.abs.casino.cassandra.persist.mp;

public class LeaderboardInfo {
    private long leaderboardId;
    private long startDate;
    private long endDate;

    public LeaderboardInfo(long leaderboardId, long startDate, long endDate) {
        this.leaderboardId = leaderboardId;
        this.startDate = startDate;
        this.endDate = endDate;
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

    @Override
    public String toString() {
        return "LeaderboardInfo{" +
                "leaderboardId=" + leaderboardId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
