package com.dgphoenix.casino.kafka.dto;

import java.util.List;

public class GetExternalAccountIdsRequest implements KafkaRequest {
    private List<Long> accountIds;

    public GetExternalAccountIdsRequest() {}

    public GetExternalAccountIdsRequest(List<Long> accountIds) {
        this.accountIds = accountIds;
    }

    public List<Long> getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(List<Long> accountIds) {
        this.accountIds = accountIds;
    }
}
