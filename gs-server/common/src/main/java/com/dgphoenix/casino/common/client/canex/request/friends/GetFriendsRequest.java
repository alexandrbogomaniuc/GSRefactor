package com.abs.casino.common.client.canex.request.friends;

import com.abs.casino.common.client.canex.request.CanexJsonRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class GetFriendsRequest extends CanexJsonRequest {

    @JsonProperty("nickname")
    @SerializedName("nickname")
    private String nickname;
    @JsonProperty("externalId")
    @SerializedName("externalId")
    private String externalId;

    public GetFriendsRequest() {
    }

    public GetFriendsRequest(String externalId, String nickname) {
        this.externalId = externalId;
        this.nickname = nickname;
    }

    public String getExternalIds() {
        return externalId;
    }

    public void setExternalIds(String externalId) {
        this.externalId = externalId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return "GetFriendsRequest{" +
                "nickname='" + nickname + '\'' +
                ", externalId='" + externalId + '\'' +
                '}';
    }
}
