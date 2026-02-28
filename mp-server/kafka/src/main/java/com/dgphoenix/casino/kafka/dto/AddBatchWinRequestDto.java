package com.abs.casino.kafka.dto;

import java.util.Set;

public class AddBatchWinRequestDto implements KafkaRequest {
    private long roomId;
    private long roundId;
    private long gameId;
    private Set<AddWinRequestDto> addWinRequest;
    private long timeoutInMillis;

    public AddBatchWinRequestDto() {}

    public AddBatchWinRequestDto(long roomId,
            long roundId,
            long gameId,
            Set<AddWinRequestDto> addWinRequest,
            long timeoutInMillis) {
        super();
        this.roomId = roomId;
        this.roundId = roundId;
        this.gameId = gameId;
        this.addWinRequest = addWinRequest;
        this.timeoutInMillis = timeoutInMillis;
    }

    public long getRoomId() {
        return roomId;
    }

    public long getRoundId() {
        return roundId;
    }

    public long getGameId() {
        return gameId;
    }

    public Set<AddWinRequestDto> getAddWinRequest() {
        return addWinRequest;
    }

    public long getTimeoutInMillis() {
        return timeoutInMillis;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public void setAddWinRequest(Set<AddWinRequestDto> addWinRequest) {
        this.addWinRequest = addWinRequest;
    }

    public void setTimeoutInMillis(long timeoutInMillis) {
        this.timeoutInMillis = timeoutInMillis;
    }
}
