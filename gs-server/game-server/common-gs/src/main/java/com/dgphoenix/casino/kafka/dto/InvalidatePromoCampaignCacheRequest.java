package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class InvalidatePromoCampaignCacheRequest implements KafkaRequest {
    private long promoCampaignId;

    public InvalidatePromoCampaignCacheRequest() {}
    
    public InvalidatePromoCampaignCacheRequest(long promoCampaignId) {
        this.promoCampaignId = promoCampaignId;
    }

    public long getPromoCampaignId() {
        return promoCampaignId;
    }

    public void setPromoCampaignId(long promoCampaignId) {
        this.promoCampaignId = promoCampaignId;
    }
}
