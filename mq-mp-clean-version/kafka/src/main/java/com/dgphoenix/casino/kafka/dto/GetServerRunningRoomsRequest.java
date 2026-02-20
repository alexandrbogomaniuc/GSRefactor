package com.dgphoenix.casino.kafka.dto;

public class GetServerRunningRoomsRequest implements KafkaRequest {
    private Long gameId;

    public GetServerRunningRoomsRequest() {}

    public GetServerRunningRoomsRequest(Long gameId) {
        this.gameId = gameId;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }
}
