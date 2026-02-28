package com.abs.casino.kafka.dto;

public class LeaveMultiPlayerLobbyRequest implements KafkaRequest {
    private String sessionId;

    public LeaveMultiPlayerLobbyRequest() {}

    public LeaveMultiPlayerLobbyRequest(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
