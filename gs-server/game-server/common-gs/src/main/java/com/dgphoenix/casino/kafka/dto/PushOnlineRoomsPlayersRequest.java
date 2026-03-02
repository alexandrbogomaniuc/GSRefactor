package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.RMSRoomDto;

import com.abs.casino.kafka.dto.KafkaRequest;

import java.util.List;

public class PushOnlineRoomsPlayersRequest implements KafkaRequest {
    private List<RMSRoomDto> roomsPlayers;

    public PushOnlineRoomsPlayersRequest() {}

    public PushOnlineRoomsPlayersRequest(List<RMSRoomDto> roomsPlayers) {
        this.roomsPlayers = roomsPlayers;
    }

    public List<RMSRoomDto> getRoomsPlayers() {
        return roomsPlayers;
    }

    public void setRoomsPlayers(List<RMSRoomDto> roomsPlayers) {
        this.roomsPlayers = roomsPlayers;
    }
}
