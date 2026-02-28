package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.BGUpdatePrivateRoomRequest;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class UpdatePlayersStatusInPrivateRoomRequest implements KafkaRequest {
    private BGUpdatePrivateRoomRequest request;

    public UpdatePlayersStatusInPrivateRoomRequest() {}

    public UpdatePlayersStatusInPrivateRoomRequest(BGUpdatePrivateRoomRequest request) {
        this.request = request;
    }

    public BGUpdatePrivateRoomRequest getRequest() {
        return request;
    }

    public void setRequest(BGUpdatePrivateRoomRequest request) {
        this.request = request;
    }
}
