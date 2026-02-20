package com.dgphoenix.casino.kafka.dto;

public class InvalidateFrBonusClientRequest implements KafkaRequest {
    private long bankId;

    public InvalidateFrBonusClientRequest() {}

    public InvalidateFrBonusClientRequest(long bankId) {
        this.bankId = bankId;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }
}
