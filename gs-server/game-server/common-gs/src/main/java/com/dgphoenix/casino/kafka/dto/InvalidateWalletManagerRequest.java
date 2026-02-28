package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class InvalidateWalletManagerRequest implements KafkaRequest {
    private long bankId;

    public InvalidateWalletManagerRequest() {}

    public InvalidateWalletManagerRequest(long bankId) {
        this.bankId = bankId;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }
}
