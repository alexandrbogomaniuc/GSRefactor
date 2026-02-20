package com.dgphoenix.casino.kafka.dto;

public class SendBalanceUpdatedRequest implements KafkaRequest {
    private String sessionId;
    private long balance;

    public SendBalanceUpdatedRequest() {}

    public SendBalanceUpdatedRequest(String sessionId, long balance) {
        this.sessionId = sessionId;
        this.balance = balance;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getBalance() {
        return balance;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }
}
