package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.KafkaRequest;

public class StartNewRoundRequest implements KafkaRequest {
    private String sessionId;
    private long accountId;
    private long gameSessionId;
    private long roomId;
    private long roomRoundId;
    private long roundStartDate;
    private boolean battlegroundRoom;
    private long stakeOrBuyInAmount;

    public StartNewRoundRequest() {}

    public StartNewRoundRequest(String sessionId,
            long accountId,
            long gameSessionId,
            long roomId,
            long roomRoundId,
            long roundStartDate,
            boolean battlegroundRoom,
            long stakeOrBuyInAmount) {
        this.sessionId = sessionId;
        this.accountId = accountId;
        this.gameSessionId = gameSessionId;
        this.roomId = roomId;
        this.roomRoundId = roomRoundId;
        this.roundStartDate = roundStartDate;
        this.battlegroundRoom = battlegroundRoom;
        this.stakeOrBuyInAmount = stakeOrBuyInAmount;
    }

    public String getSessionId() {
        return sessionId;
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

    public long getRoomRoundId() {
        return roomRoundId;
    }

    public long getRoundStartDate() {
        return roundStartDate;
    }

    public boolean isBattlegroundRoom() {
        return battlegroundRoom;
    }

    public long getStakeOrBuyInAmount() {
        return stakeOrBuyInAmount;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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

    public void setRoomRoundId(long roomRoundId) {
        this.roomRoundId = roomRoundId;
    }

    public void setRoundStartDate(long roundStartDate) {
        this.roundStartDate = roundStartDate;
    }

    public void setBattlegroundRoom(boolean battlegroundRoom) {
        this.battlegroundRoom = battlegroundRoom;
    }

    public void setStakeOrBuyInAmount(long stakeOrBuyInAmount) {
        this.stakeOrBuyInAmount = stakeOrBuyInAmount;
    }
}
