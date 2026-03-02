package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.KafkaRequest;

public class InvalidateFrBonusWinManagerRequest implements KafkaRequest {
    private long bankId;

    public InvalidateFrBonusWinManagerRequest() {}

    public InvalidateFrBonusWinManagerRequest(long bankId) {
        this.bankId = bankId;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }
}
