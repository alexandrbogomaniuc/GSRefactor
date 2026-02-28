package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

import java.util.List;

public class BGUpdatePrivateRoomRequest implements KafkaRequest {
    private String privateRoomId;
    private List<BGPlayerDto> players;
    private int bankId;

    public BGUpdatePrivateRoomRequest() {}

    public BGUpdatePrivateRoomRequest(String privateRoomId, List<BGPlayerDto> players, int bankId) {
        this.privateRoomId = privateRoomId;
        this.players = players;
        this.bankId = bankId;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public List<BGPlayerDto> getPlayers() {
        return players;
    }

    public int getBankId() {
        return bankId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    public void setPlayers(List<BGPlayerDto> players) {
        this.players = players;
    }

    public void setBankId(int bankId) {
        this.bankId = bankId;
    }
}
