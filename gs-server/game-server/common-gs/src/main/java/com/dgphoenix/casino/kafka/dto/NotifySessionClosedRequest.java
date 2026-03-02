package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.KafkaRequest;

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
