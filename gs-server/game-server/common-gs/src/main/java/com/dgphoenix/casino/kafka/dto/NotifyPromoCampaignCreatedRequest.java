package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class NotifyPromoCampaignCreatedRequest implements KafkaRequest {
    private long promoCampaignId;

    public NotifyPromoCampaignCreatedRequest() {}

    public NotifyPromoCampaignCreatedRequest(long promoCampaignId) {
        this.promoCampaignId = promoCampaignId;
    }

    public long getPromoCampaignId() {
        return promoCampaignId;
    }

    public void setPromoCampaignId(long promoCampaignId) {
        this.promoCampaignId = promoCampaignId;
    }
}
