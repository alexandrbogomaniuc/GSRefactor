package com.dgphoenix.casino.common.client.canex.request.onlinerooms;

import com.dgphoenix.casino.common.client.canex.request.CanexJsonRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class Player extends CanexJsonRequest {

    @JsonProperty("nickname")
    @SerializedName("nickname")
    String nickname;

    @JsonProperty("isOwner")
    @SerializedName("isOwner")
    boolean isOwner;

    @JsonProperty("externalId")
    @SerializedName("externalId")
    String externalId;

    public Player() {
    }

    public Player(String nickname, boolean isOwner, String externalId) {
        this.nickname = nickname;
        this.isOwner = isOwner;
        this.externalId = externalId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public boolean getOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String sessionId) {
        this.externalId = sessionId;
    }

    @Override
    public String toString() {
        return "Player{" +
                ", nickname='" + nickname + '\'' +
                ", isOwner=" + isOwner +
                ", externalId='" + externalId + '\'' +
                '}';
    }
}
