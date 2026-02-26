package com.dgphoenix.casino.common.client.canex.request.friends;

import com.abs.casino.common.client.canex.request.CanexJsonRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UpdateFriendsRequest extends CanexJsonRequest {
    @JsonProperty("nickname")
    @SerializedName("nickname")
    private String nickname;

    @JsonProperty("externalId")
    @SerializedName("externalId")
    private String externalId;

    @JsonProperty("friends")
    @SerializedName("friends")
    private List<Friend> friends;

    // Default constructor
    public UpdateFriendsRequest() {
    }

    public UpdateFriendsRequest(String nickname, String externalId, List<Friend> friends) {
        this.nickname = nickname;
        this.externalId = externalId;
        this.friends = friends;
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

    public List<Friend> getFriends() {
        return friends;
    }

    public void setFriends(List<Friend> friends) {
        this.friends = friends;
    }

    @Override
    public String toString() {
        return "UpdateFriendsRequest{" +
                "nickname='" + nickname + '\'' +
                ", externalId='" + externalId + '\'' +
                ", friends=" + friends +
                '}';
    }
}
