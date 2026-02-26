package com.dgphoenix.casino.common.client.canex.request.privateroom;

import com.abs.casino.common.client.canex.request.CanexJsonRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class Player extends CanexJsonRequest {

    @JsonProperty("nickname")
    @SerializedName("nickname")
    private String nickname;

    @JsonProperty("externalId")
    @SerializedName("externalId")
    private String externalId;

    @JsonProperty("status")
    @SerializedName("status")
    private Status status;

    public Player() {
    }

    public Player(String nickname, String externalId, Status status) {
        this.nickname = nickname;
        this.externalId = externalId;
        this.status = status;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Player{" +
                "nickname='" + nickname + '\'' +
                ", externalId='" + externalId + '\'' +
                ", status=" + status +
                '}';
    }
}
