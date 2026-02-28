package com.abs.casino.kafka.dto.bots.response;

import com.abs.casino.kafka.dto.BasicKafkaResponse;

public class BotStatusResponse extends BasicKafkaResponse {
    private int status; 
    private long mmcBalance; 
    private long mqcBalance; 

    public BotStatusResponse(){}

    public BotStatusResponse(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public BotStatusResponse(int status, long mmcBalance, long mqcBalance, boolean success, int errorCode, String errorDetails) {
        super(success, errorCode, errorDetails);
        this.status = status;
        this.mmcBalance = mmcBalance;
        this.mqcBalance = mqcBalance;
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

    @Override
    public String toString() {
        return "BotStatusResponse{" +
                "status=" + status +
                ", mmcBalance=" + mmcBalance +
                ", mqcBalance=" + mqcBalance + '\'' +
                '}';
    }
}
