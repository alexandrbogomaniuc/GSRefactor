package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.KafkaRequest;

public class FinishGameSessionAndMakeSitOutRequest implements KafkaRequest {
    private String sid;
    private String privateRoomId;

    public FinishGameSessionAndMakeSitOutRequest() {}

    public FinishGameSessionAndMakeSitOutRequest(String sid, String privateRoomId) {
        this.sid = sid;
        this.privateRoomId = privateRoomId;
    }

    public String getSid() {
        return sid;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }
}
