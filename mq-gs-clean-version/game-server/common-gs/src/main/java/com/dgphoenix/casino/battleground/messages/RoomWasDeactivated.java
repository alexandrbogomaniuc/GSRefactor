package com.dgphoenix.casino.battleground.messages;

public class RoomWasDeactivated {
    private String privateRoomId;
    private String reason;

    public RoomWasDeactivated(String privateRoomId, String reason) {
        this.privateRoomId = privateRoomId;
        this.reason = reason;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "RoomWasDeactivated{" +
                "privateRoomId='" + privateRoomId + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
