package com.betsoft.casino.mp.model.bots.dto;

import java.util.List;

public class BotsMap {
    private boolean success;
    private int statusCode;
    private String reasonPhrases;
    private List<SimpleBot> botsMap;

    public BotsMap() {
    }

    public BotsMap(List<SimpleBot> botsMap,
            boolean success,
            int statusCode,
            String reasonPhrases) {
        this.success = success;
        this.statusCode = statusCode;
        this.reasonPhrases = reasonPhrases;
        this.botsMap = botsMap;
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

    public List<SimpleBot> getBotsMap() {
        return botsMap;
    }

    public void setBotsMap(List<SimpleBot> botsMap) {
        this.botsMap = botsMap;
    }
}
