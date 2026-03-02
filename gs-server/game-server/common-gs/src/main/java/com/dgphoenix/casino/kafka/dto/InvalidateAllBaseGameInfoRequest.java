package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.KafkaRequest;

public class InvalidateAllBaseGameInfoRequest implements KafkaRequest {
    private long bankId;

    public InvalidateAllBaseGameInfoRequest() {}

    public InvalidateAllBaseGameInfoRequest(long bankId) {
        this.bankId = bankId;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }
}
