package com.abs.casino.kafka.dto.privateroom.request;

import java.util.List;

import com.abs.casino.kafka.dto.BGPlayerDto;
import com.abs.casino.kafka.dto.KafkaRequest;

public class UpdateRoomDto implements KafkaRequest {
    private String privateRoomId;
    private List<BGPlayerDto> players;
    private int bankId;
    private boolean isTransitionLimited;

    public UpdateRoomDto(){}

    public UpdateRoomDto(String privateRoomId, List<BGPlayerDto> players, int bankId, boolean isTransitionLimited) {
        super();
        this.privateRoomId = privateRoomId;
        this.players = players;
        this.bankId = bankId;
        this.isTransitionLimited = isTransitionLimited;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    public List<BGPlayerDto> getPlayers() {
        return players;
    }

    public void setPlayers(List<BGPlayerDto> players) {
        this.players = players;
    }

    public int getBankId() {
        return bankId;
    }

    public void setBankId(int bankId) {
        this.bankId = bankId;
    }

    public boolean isTransitionLimited() {
        return isTransitionLimited;
    }

    public void setTransitionLimited(boolean transitionLimited) {
        isTransitionLimited = transitionLimited;
    }
}
