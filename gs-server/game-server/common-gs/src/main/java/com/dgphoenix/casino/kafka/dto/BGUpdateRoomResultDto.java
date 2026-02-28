package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;

import java.util.List;

public class BGUpdateRoomResultDto extends BasicKafkaResponse {
    private int code;
    private String message;
    private String privateRoomId;
    private List<BGPlayerDto> players;

    public BGUpdateRoomResultDto() {}

    public BGUpdateRoomResultDto(boolean success, int statusCode, String reasonPhrases, int code, String message, String privateRoomId, List<BGPlayerDto> players) {
        super(success, statusCode, reasonPhrases);
        this.code = code;
        this.message = message;
        this.privateRoomId = privateRoomId;
        this.players = players;
    }

    public BGUpdateRoomResultDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public BGUpdateRoomResultDto(int code, String message, String privateRoomId, List<BGPlayerDto> players) {
        super(true, 0, "");
        this.code = code;
        this.message = message;
        this.privateRoomId = privateRoomId;
        this.players = players;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public List<BGPlayerDto> getPlayers() {
        return players;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    public void setPlayers(List<BGPlayerDto> players) {
        this.players = players;
    }

}
