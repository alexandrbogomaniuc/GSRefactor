package com.dgphoenix.casino.kafka.dto;

public abstract class BasicKafkaResponse implements KafkaResponse {
    private boolean success;
    private int statusCode;
    private String reasonPhrases;

    public BasicKafkaResponse() { }

    public BasicKafkaResponse(boolean success, int statusCode, String reasonPhrases) {
        this.success = success;
        this.statusCode = statusCode;
        this.reasonPhrases = reasonPhrases;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String getReasonPhrases() {
        return reasonPhrases;
    }

    public void setReasonPhrases(String reasonPhrases) {
        this.reasonPhrases = reasonPhrases;
    }
}