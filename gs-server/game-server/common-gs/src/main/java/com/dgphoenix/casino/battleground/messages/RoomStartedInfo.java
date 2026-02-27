package com.abs.casino.battleground.messages;

public class RoomStartedInfo {
    private String sid;
    private String privateRoomId;
    private String userId;
    private Long startTime;

    public RoomStartedInfo(String sid, String privateRoomId, String userId, Long startTime) {
        this.sid = sid;
        this.privateRoomId = privateRoomId;
        this.userId = userId;
        this.startTime = startTime;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "RoomStartedInfo{" +
                "sid='" + sid + '\'' +
                ", privateRoomId='" + privateRoomId + '\'' +
                ", userId='" + userId + '\'' +
                ", startTime=" + startTime +
                '}';
    }
}
