package com.dgphoenix.casino.common.client.canex.request.privateroom;

import com.abs.casino.common.client.canex.request.CanexJsonRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PrivateRoom extends CanexJsonRequest {

    @JsonProperty("bankId")
    @SerializedName("bankId")
    private Long bankId;

    @JsonProperty("privateRoomId")
    @SerializedName("privateRoomId")
    private String privateRoomId;

    @JsonProperty("players")
    @SerializedName("players")
    private List<Player> players;

    public PrivateRoom() {
    }

    public PrivateRoom(Long bankId, String privateRoomId, List<Player> players) {
        this.bankId = bankId;
        this.privateRoomId = privateRoomId;
        this.players = players;
    }

    public Long getBankId() {
        return bankId;
    }

    public void setBankId(Long bankId) {
        this.bankId = bankId;
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
        return "PrivateRoom{" +
                "bankId=" + bankId +
                ", privateRoomId='" + privateRoomId + '\'' +
                ", players=" + players +
                '}';
    }
}
