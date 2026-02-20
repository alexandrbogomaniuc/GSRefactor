package com.betsoft.casino.bots;

public class BotParams {

    private String socketUrl;
    private String sessionId;
    private int serverId;

    public BotParams(String socketUrl, String sessionId, int serverId) {
        this.socketUrl = socketUrl;
        this.sessionId = sessionId;
        this.serverId = serverId;
    }

    public String getSocketUrl() {
        return socketUrl;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getServerId() {
        return serverId;
    }

    @Override
    public String toString() {
        return "BotParams{" +
                "socketUrl='" + socketUrl + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", serverId=" + serverId +
                '}';
    }
}
