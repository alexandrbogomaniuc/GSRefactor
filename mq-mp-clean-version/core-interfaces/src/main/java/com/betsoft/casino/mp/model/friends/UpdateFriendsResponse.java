package com.betsoft.casino.mp.model.friends;

import com.dgphoenix.casino.common.util.string.StringUtils;

public class UpdateFriendsResponse {
    private int code;
    private String message;
    private Friends friends;

    public UpdateFriendsResponse() {

    }

    public UpdateFriendsResponse(int code, String message, Friends friends) {
        this.code = code;
        this.message = message;
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

    public void addMessage(String message) {
        if(StringUtils.isTrimmedEmpty(this.message)) {
            this.message = message;
        } else {
            this.message += ";" + message;
        }
    }

    public Friends getFriends() {
        return friends;
    }

    public void setFriends(Friends friends) {
        this.friends = friends;
    }

    @Override
    public String toString() {
        return "UpdateFriendsResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", friends=" + friends +
                '}';
    }
}
