package com.dgphoenix.casino.kafka.dto;

public class NotifySessionClosedRequest implements KafkaRequest {
    private String sessionId;

    public NotifySessionClosedRequest() {}

    public NotifySessionClosedRequest(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

}
