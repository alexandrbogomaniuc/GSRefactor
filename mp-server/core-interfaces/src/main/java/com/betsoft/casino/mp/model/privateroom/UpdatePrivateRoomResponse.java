package com.betsoft.casino.mp.model.privateroom;

import com.dgphoenix.casino.common.util.string.StringUtils;

public class UpdatePrivateRoomResponse {
    private int code;
    private String message;
    private PrivateRoom privateRoom;

    public UpdatePrivateRoomResponse() {

    }

    public UpdatePrivateRoomResponse(int code, String message, PrivateRoom privateRoom) {
        this.code = code;
        this.message = message;
        this.privateRoom = privateRoom;
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

    public PrivateRoom getPrivateRoom() {
        return privateRoom;
    }

    public void setPrivateRoom(PrivateRoom privateRoom) {
        this.privateRoom = privateRoom;
    }

    @Override
    public String toString() {
        return "UpdatePrivateRoomResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", privateRoom=" + privateRoom +
                '}';
    }
}
