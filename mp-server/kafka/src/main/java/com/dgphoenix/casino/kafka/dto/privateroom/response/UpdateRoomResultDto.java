package com.abs.casino.kafka.dto.privateroom.response;

import java.util.List;

import com.abs.casino.kafka.dto.BGPlayerDto;
import com.abs.casino.kafka.dto.BasicKafkaResponse;

public class UpdateRoomResultDto extends BasicKafkaResponse {
    private String privateRoomId;
    private List<BGPlayerDto> players;

    public UpdateRoomResultDto(){}

    public UpdateRoomResultDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public UpdateRoomResultDto(String privateRoomId, List<BGPlayerDto> players) {
        this.privateRoomId = privateRoomId;
        this.players = players;
    }

    public UpdateRoomResultDto(boolean success, int statusCode, String reasonPhrases, String privateRoomId, List<BGPlayerDto> players) {
        super(success, statusCode, reasonPhrases);
        this.privateRoomId = privateRoomId;
        this.players = players;
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
}
