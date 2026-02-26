package com.abs.casino.controller.mqb.response;

import com.dgphoenix.casino.common.client.canex.request.friends.Friend;

import java.util.List;

public class PostFriendsResponse extends BaseResult {

    private String nickname;

    private String externalId;

    private List<Friend> friends;

    public PostFriendsResponse(String result, String message, String nickname, String externalId, List<Friend> friends) {
        super(result, message);
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
}
