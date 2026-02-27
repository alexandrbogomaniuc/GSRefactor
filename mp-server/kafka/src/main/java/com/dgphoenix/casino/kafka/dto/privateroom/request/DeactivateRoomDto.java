package com.abs.casino.kafka.dto.privateroom.request;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class DeactivateRoomDto implements KafkaRequest {
    private String ownerUsername;
    private long ownerAccountId;
    private String roomId;
    private String ownerExternalId;

    public DeactivateRoomDto(){}

    public DeactivateRoomDto(String ownerUsername, long ownerAccountId, String roomId, String ownerExternalId) {
        this.ownerUsername = ownerUsername;
        this.ownerAccountId = ownerAccountId;
        this.roomId = roomId;
        this.ownerExternalId = ownerExternalId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public long getOwnerAccountId() {
        return ownerAccountId;
    }

    public void setOwnerAccountId(long ownerAccountId) {
        this.ownerAccountId = ownerAccountId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getOwnerExternalId() {
        return ownerExternalId;
    }

    public void setOwnerExternalId(String ownerExternalId) {
        this.ownerExternalId = ownerExternalId;
    }
}
