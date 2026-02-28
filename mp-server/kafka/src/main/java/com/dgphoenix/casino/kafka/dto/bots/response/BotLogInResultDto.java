package com.abs.casino.kafka.dto.bots.response;

import com.abs.casino.kafka.dto.BasicKafkaResponse;

public class BotLogInResultDto extends BasicKafkaResponse {
    private String sessionId;
    private long mmcBalance;
    private long mqcBalance;

    public BotLogInResultDto(){}

    public BotLogInResultDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public BotLogInResultDto(String sessionId, long mmcBalance, long mqcBalance, boolean success, int errorCode, String errorDetails) {
        super(success, errorCode, errorDetails);
        this.sessionId = sessionId;
        this.mmcBalance = mmcBalance;
        this.mqcBalance = mqcBalance;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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
                "sessionId=" + sessionId +
                ", mmcBalance=" + mmcBalance +
                ", mqcBalance=" + mqcBalance + '\'' +
                '}';
    }
}
