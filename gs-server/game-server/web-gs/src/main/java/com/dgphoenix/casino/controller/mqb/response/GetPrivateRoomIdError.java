package com.abs.casino.controller.mqb.response;

import java.util.List;

public class GetPrivateRoomIdError extends BaseResult {

    private List<String> activePrivateRooms;

    public GetPrivateRoomIdError(String result, String message, List<String> activePrivateRooms) {
        super(result, message);
        this.activePrivateRooms = activePrivateRooms;
    }

    public List<String> getActivePrivateRooms() {
        return activePrivateRooms;
    }

    public void setActivePrivateRooms(List<String> activePrivateRooms) {
        this.activePrivateRooms = activePrivateRooms;
    }
}
