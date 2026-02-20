package com.betsoft.casino.mp.model;

public class RMSPlayer implements IRMSPlayer {
    private int serverId;
    private String nickname;
    private boolean isOwner;
    private String sessionId;
    private int seatNr;

    public RMSPlayer() {}

    public RMSPlayer(int serverId,
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

    @Override
    public int getServerId() {
        return serverId;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public boolean isIsOwner() {
        return isOwner;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public int getSeatNr() {
        return seatNr;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setIsOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setSeatNr(int seatNr) {
        this.seatNr = seatNr;
    }
}
