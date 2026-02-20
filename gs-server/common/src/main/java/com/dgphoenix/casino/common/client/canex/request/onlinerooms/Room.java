package com.dgphoenix.casino.common.client.canex.request.onlinerooms;

import com.dgphoenix.casino.common.client.canex.request.CanexJsonRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Room extends CanexJsonRequest {
    @JsonProperty("roomId")
    @SerializedName("roomId")
    Long roomId;

    @JsonProperty("serverId")
    @SerializedName("serverId")
    Integer serverId;

    @JsonProperty("isBattleground")
    @SerializedName("isBattleground")
    boolean isBattleground;

    @JsonProperty("isPrivate")
    @SerializedName("isPrivate")
    boolean isPrivate;

    @JsonProperty("buyInStake")
    @SerializedName("buyInStake")
    Long buyInStake;

    @JsonProperty("currency")
    @SerializedName("currency")
    String currency;

    @JsonProperty("gameId")
    @SerializedName("gameId")
    Long gameId;

    @JsonProperty("gameName")
    @SerializedName("gameName")
    String gameName;

    @JsonProperty("players")
    @SerializedName("players")
    List<Player> players;

    public Room() {
    }

    public Room(Long roomId, Integer serverId, boolean isBattleground, boolean isPrivate,
                Long buyInStake, String currency, Long gameId, String gameName, List<Player> players) {
        this.roomId = roomId;
        this.serverId = serverId;
        this.isBattleground = isBattleground;
        this.isPrivate = isPrivate;
        this.buyInStake = buyInStake;
        this.currency = currency;
        this.gameId = gameId;
        this.gameName = gameName;
        this.players = players;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public boolean isBattleground() {
        return isBattleground;
    }

    public void setBattleground(boolean battleground) {
        isBattleground = battleground;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public Long getBuyInStake() {
        return buyInStake;
    }

    public void setBuyInStake(Long buyInStake) {
        this.buyInStake = buyInStake;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomId=" + roomId +
                ", serverId=" + serverId +
                ", isBattleground=" + isBattleground +
                ", isPrivate=" + isPrivate +
                ", buyInStake=" + buyInStake +
                ", currency='" + currency + '\'' +
                ", gameId=" + gameId +
                ", gameName='" + gameName + '\'' +
                ", players=" + players +
                '}';
    }
}
