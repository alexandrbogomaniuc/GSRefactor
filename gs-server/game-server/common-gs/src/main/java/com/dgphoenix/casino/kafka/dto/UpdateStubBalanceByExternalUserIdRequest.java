package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class UpdateStubBalanceByExternalUserIdRequest implements KafkaRequest {
    private String externalUserId;
    private long balance;

    public UpdateStubBalanceByExternalUserIdRequest() {}

    public UpdateStubBalanceByExternalUserIdRequest(String externalUserId, long balance) {
        this.externalUserId = externalUserId;
        this.balance = balance;
    }

    public String getExternalUserId() {
        return externalUserId;
    }

    public long getBalance() {
        return balance;
    }

    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

}
