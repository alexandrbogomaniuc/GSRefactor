package com.betsoft.casino.mp.model;

import java.util.List;

public class BGUpdatePrivateRoom implements IBGUpdatePrivateRoom {
    private String privateRoomId;
    private List<IBGPlayer> players;
    private int bankId;

    public BGUpdatePrivateRoom() {}

    public BGUpdatePrivateRoom(String privateRoomId, List<IBGPlayer> players, int bankId) {
        this.privateRoomId = privateRoomId;
        this.players = players;
        this.bankId = bankId;
    }

    @Override
    public String getPrivateRoomId() {
        return privateRoomId;
    }

    @Override
    public List<IBGPlayer> getPlayers() {
        return players;
    }

    @Override
    public int getBankId() {
        return bankId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    public void setPlayers(List<IBGPlayer> players) {
        this.players = players;
    }

    public void setBankId(int bankId) {
        this.bankId = bankId;
    }
}
