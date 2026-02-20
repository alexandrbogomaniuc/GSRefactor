package com.dgphoenix.casino.kafka.dto.privateroom.response;

import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;

public class RoomInfoResultDto extends BasicKafkaResponse {
    private boolean privateRoom;
    private String privateRoomId;

    public RoomInfoResultDto() {}

    public RoomInfoResultDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public RoomInfoResultDto(boolean success, int statusCode, String reasonPhrases, boolean privateRoom, String privateRoomId) {
        super(success, statusCode, reasonPhrases);
        this.privateRoom = privateRoom;
        this.privateRoomId = privateRoomId;
    }

    public boolean isPrivateRoom() {
        return privateRoom;
    }

    public void setPrivateRoom(boolean privateRoom) {
        this.privateRoom = privateRoom;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }
}
