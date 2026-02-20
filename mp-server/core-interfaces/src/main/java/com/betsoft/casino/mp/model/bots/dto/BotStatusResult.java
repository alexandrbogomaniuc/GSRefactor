package com.betsoft.casino.mp.model.bots.dto;

public class BotStatusResult {
    private int status;
    private long mmcBalance;
    private long mqcBalance;
    private boolean success;
    private int statusCode;
    private String reasonPhrases;

    public BotStatusResult(){}

    public BotStatusResult(int status, long mmcBalance, long mqcBalance, boolean success, int statusCode, String reasonPhrases) {
        this.status = status;
        this.mmcBalance = mmcBalance;
        this.mqcBalance = mqcBalance;
        this.success = success;
        this.statusCode = statusCode;
        this.reasonPhrases = reasonPhrases;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getMmcBalance() {
        return mmcBalance;
    }

    public void setMmcBalance(long mmcBalance) {
        this.mmcBalance = mmcBalance;
    }

    public long getMqcBalance() {
        return mqcBalance;
    }

    public void setMqcBalance(long mqcBalance) {
        this.mqcBalance = mqcBalance;
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
