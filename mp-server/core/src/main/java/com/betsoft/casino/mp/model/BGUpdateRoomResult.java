package com.betsoft.casino.mp.model;

import java.util.List;

public class BGUpdateRoomResult implements IBGUpdateRoomResult {
    private int code;
    private String message;
    private String privateRoomId;
    private List<IBGPlayer> players;

    public BGUpdateRoomResult() {}

    public BGUpdateRoomResult(int code, String message, String privateRoomId, List<IBGPlayer> players) {
        this.code = code;
        this.message = message;
        this.privateRoomId = privateRoomId;
        this.players = players;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getPrivateRoomId() {
        return privateRoomId;
    }

    @Override
    public List<IBGPlayer> getPlayers() {
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

    public void setPlayers(List<IBGPlayer> players) {
        this.players = players;
    }

}
