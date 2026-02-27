package com.abs.casino.common.client.canex.request.onlineplayer;

import com.abs.casino.common.client.canex.request.CanexJsonRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class OnlinePlayer extends CanexJsonRequest {
    @JsonProperty("nickname")
    @SerializedName("nickname")
    private String nickname;

    @JsonProperty("externalId")
    @SerializedName("externalId")
    private String externalId;

    @JsonProperty("online")
    @SerializedName("online")
    private boolean online;

    public OnlinePlayer() {
    }

    public OnlinePlayer(String nickname, String externalId, boolean online) {
        this.nickname = nickname;
        this.externalId = externalId;
        this.online = online;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public String toString() {
        return "OnlinePlayer{" +
                "nickname='" + nickname + '\'' +
                ", externalId='" + externalId + '\'' +
                ", online=" + online +
                '}';
    }
}
