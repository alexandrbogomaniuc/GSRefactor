package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.BGPlayerDto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

import java.util.List;

public class InvitePlayersToPrivateRoomRequest implements KafkaRequest {
    private List<BGPlayerDto> players;
    private String privateRoomId;

    public InvitePlayersToPrivateRoomRequest() {}

    public InvitePlayersToPrivateRoomRequest(List<BGPlayerDto> players, String privateRoomId) {
        this.players = players;
        this.privateRoomId = privateRoomId;
    }

    public List<BGPlayerDto> getPlayers() {
        return players;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPlayers(List<BGPlayerDto> players) {
        this.players = players;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }
}
