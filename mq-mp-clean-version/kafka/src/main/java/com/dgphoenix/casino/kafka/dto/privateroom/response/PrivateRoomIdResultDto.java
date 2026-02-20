package com.dgphoenix.casino.kafka.dto.privateroom.response;

import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;

import java.util.List;

public class PrivateRoomIdResultDto extends BasicKafkaResponse {
    private String privateRoomId;
    private List<String> activePrivateRooms;

    public PrivateRoomIdResultDto(){}

    public PrivateRoomIdResultDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public PrivateRoomIdResultDto(boolean success, int statusCode, String reasonPhrases, String privateRoomId, List<String> activePrivateRooms) {
        super(success, statusCode, reasonPhrases);
        this.privateRoomId = privateRoomId;
        this.activePrivateRooms = activePrivateRooms;
    }

    public PrivateRoomIdResultDto(String privateRoomId, List<String> activePrivateRooms) {
        super(true, 0, "");
        this.privateRoomId = privateRoomId;
        this.activePrivateRooms = activePrivateRooms;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public List<String> getActivePrivateRooms() {
        return activePrivateRooms;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    public void setActivePrivateRooms(List<String> activePrivateRooms) {
        this.activePrivateRooms = activePrivateRooms;
    }
}

