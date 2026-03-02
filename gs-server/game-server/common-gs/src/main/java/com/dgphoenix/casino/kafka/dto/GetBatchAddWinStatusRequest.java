package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.KafkaRequest;

public class GetBatchAddWinStatusRequest implements KafkaRequest {
    private long roomId;
    private long roundId;

    public GetBatchAddWinStatusRequest() {}

    public GetBatchAddWinStatusRequest(long roomId, long roundId) {
        super();
        this.roomId = roomId;
        this.roundId = roundId;
    }

    public long getRoomId() {
        return roomId;
    }

    public long getRoundId() {
        return roundId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }
}
