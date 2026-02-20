package com.betsoft.casino.mp.model.bots.dto;

public class BotLogOutResult {
    private boolean success;
    private int statusCode;
    private String reasonPhrases;

    public BotLogOutResult(){}

    public BotLogOutResult(boolean success, int statusCode, String reasonPhrases) {
        this.success = success;
        this.statusCode = statusCode;
        this.reasonPhrases = reasonPhrases;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getReasonPhrases() {
        return reasonPhrases;
    }

    public void setReasonPhrases(String reasonPhrases) {
        this.reasonPhrases = reasonPhrases;
    }

}
