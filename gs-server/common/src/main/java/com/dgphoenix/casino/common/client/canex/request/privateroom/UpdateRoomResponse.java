package com.abs.casino.common.client.canex.request.privateroom;

import java.util.List;

public class UpdateRoomResponse {

    private int code;
    private String message;
    private String privateRoomId;

    private List<Player> players;

    public UpdateRoomResponse(int code, String message, String privateRoomId, List<Player> players) {
        this.code = code;
        this.message = message;
        this.privateRoomId = privateRoomId;
        this.players = players;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    @Override
    public String toString() {
        return "UpdatePrivateRoomResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", privateRoomId='" + privateRoomId + '\'' +
                ", players=" + players +
                '}';
    }
}
