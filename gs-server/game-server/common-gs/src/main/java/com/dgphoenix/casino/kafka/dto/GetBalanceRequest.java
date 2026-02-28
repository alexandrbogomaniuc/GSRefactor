package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class GetBalanceRequest implements KafkaRequest {
    private String sessionId;
    private String mode;

    public GetBalanceRequest() {}

    public GetBalanceRequest(String sessionId, String mode) {
        this.sessionId = sessionId;
        this.mode = mode;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getMode() {
        return mode;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
