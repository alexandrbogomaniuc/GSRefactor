package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class GetPrivateRoomInfoRequest implements KafkaRequest{
    private String privateRoomId;

    public GetPrivateRoomInfoRequest() {}

    public GetPrivateRoomInfoRequest(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }
}
