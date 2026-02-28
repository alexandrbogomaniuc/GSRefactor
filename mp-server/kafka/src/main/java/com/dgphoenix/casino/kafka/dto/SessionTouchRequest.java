package com.abs.casino.kafka.dto;

public class SessionTouchRequest implements KafkaRequest {
    private String sessionId;

    public SessionTouchRequest() {}

    public SessionTouchRequest(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
