package com.abs.casino.kafka.dto;

public class RMSPlayerDto {
    private int serverId;
    private String nickname;
    private boolean isOwner;
    private String sessionId;
    private int seatNr;

    public RMSPlayerDto() {}

    public RMSPlayerDto(int serverId,
            String nickname,
            boolean isOwner,
            String sessionId,
            int seatNr) {
        this.serverId = serverId;
        this.nickname = nickname;
        this.isOwner = isOwner;
        this.sessionId = sessionId;
        this.seatNr = seatNr;
    }

    public int getServerId() {
        return serverId;
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getSeatNr() {
        return seatNr;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setSeatNr(int seatNr) {
        this.seatNr = seatNr;
    }
}
