package com.abs.casino.battleground.messages;

import java.util.Set;

public class GameRoundEntry {
    private long roundId;
    private Set<String> users;
    private long startTime;
    private long endTime;

    public GameRoundEntry(long roundId, Set<String> users, long startTime, long endTime) {
        this.roundId = roundId;
        this.users = users;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "GameRoundEntry{" +
                "roundId=" + roundId +
                ", users=" + users +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
