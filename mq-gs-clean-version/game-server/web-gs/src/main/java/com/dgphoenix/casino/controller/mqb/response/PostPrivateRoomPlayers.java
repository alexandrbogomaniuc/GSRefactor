package com.dgphoenix.casino.controller.mqb.response;

import com.dgphoenix.casino.common.client.canex.request.privateroom.Player;

import java.util.List;

public class PostPrivateRoomPlayers extends BaseResult {

    private String privateRoomId;
    private List<Player> players;

    public PostPrivateRoomPlayers(String result, String message, String privateRoomId, List<Player> players) {
        super(result, message);
        this.privateRoomId = privateRoomId;
        this.players = players;
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
}
