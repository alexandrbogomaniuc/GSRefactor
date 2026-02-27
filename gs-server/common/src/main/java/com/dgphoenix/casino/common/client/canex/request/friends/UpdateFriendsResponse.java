package com.abs.casino.common.client.canex.request.friends;

import java.util.List;

public class UpdateFriendsResponse {

    private int code;
    private String message;
    private String nickname;

    private String externalId;

    private List<Friend> friends;

    public UpdateFriendsResponse(int code, String message, String nickname, String externalId, List<Friend> friends) {
        this.code = code;
        this.message = message;
        this.nickname = nickname;
        this.externalId = externalId;
        this.friends = friends;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
        return "UpdateFriendsResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", nickname='" + nickname + '\'' +
                ", externalId='" + externalId + '\'' +
                ", friends=" + friends +
                '}';
    }
}
