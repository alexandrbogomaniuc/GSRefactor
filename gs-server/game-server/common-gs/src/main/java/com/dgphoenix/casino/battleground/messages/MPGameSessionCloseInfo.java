package com.abs.casino.battleground.messages;

import java.util.List;

public class MPGameSessionCloseInfo {
    private final long gameId;
    private final long gameSessionId;
    private final List<GameRoundEntry> gameRounds;
    private final String userId;
    private final long startTime;
    private final Long endTime;
    private final String sid;
    private final String privateRoomId;

    private MPGameSessionCloseInfo(MPGameSessionCloseInfoBuilder builder) {
        this.gameId = builder.gameId;
        this.gameSessionId = builder.gameSessionId;
        this.gameRounds = builder.gameRounds;
        this.userId = builder.userId;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.sid = builder.sid;
        this.privateRoomId = builder.privateRoomId;
    }

    public long getGameId() {
        return gameId;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public List<GameRoundEntry> getGameRounds() {
        return gameRounds;
    }

    public String getUserId() {
        return userId;
    }

    public long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public String getSid() {
        return sid;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    @Override
    public String toString() {
        return "MPGameSessionCloseInfo{" +
                "gameId=" + gameId +
                ", gameSessionId=" + gameSessionId +
                ", gameRounds=" + gameRounds +
                ", userId='" + userId + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", sid=" + sid +
                ", privateRoomId=" + privateRoomId +
                '}';
    }

    public static class MPGameSessionCloseInfoBuilder {
        private final long gameId;
        private final long gameSessionId;
        private final List<GameRoundEntry> gameRounds;
        private final String userId;
        private long startTime;
        private Long endTime;
        private String sid;
        private String privateRoomId;

        public MPGameSessionCloseInfoBuilder(long gameId, long gameSessionId, List<GameRoundEntry> gameRounds,
                                             String userId) {
            this.gameId = gameId;
            this.gameSessionId = gameSessionId;
            this.gameRounds = gameRounds;
            this.userId = userId;
        }

        public MPGameSessionCloseInfoBuilder setStartTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public MPGameSessionCloseInfoBuilder setEndTime(Long endTime) {
            this.endTime = endTime;
            return this;
        }

        public MPGameSessionCloseInfoBuilder setSid(String sid) {
            this.sid = sid;
            return this;
        }

        public MPGameSessionCloseInfoBuilder setPrivateRoomId(String privateRoomId) {
            this.privateRoomId = privateRoomId;
            return this;
        }

        public MPGameSessionCloseInfo build() {
            return new MPGameSessionCloseInfo(this);
        }
    }
}
