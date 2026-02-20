package com.dgphoenix.casino.kafka.dto;

public class InvalidateBonusClientRequest implements KafkaRequest {
    private long bankId;

    public InvalidateBonusClientRequest() {}

    public InvalidateBonusClientRequest(long bankId) {
        this.bankId = bankId;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }
}
