package com.dgphoenix.casino.kafka.dto;

public class NotifyPromoCampaignStatusChangedRequest implements KafkaRequest {
    private long promoCampaignId;
    private String oldStatus;
    private String newStatus;

    public NotifyPromoCampaignStatusChangedRequest() {}

    public NotifyPromoCampaignStatusChangedRequest(long promoCampaignId,
            String oldStatus,
            String newStatus) {
        this.promoCampaignId = promoCampaignId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public long getPromoCampaignId() {
        return promoCampaignId;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setPromoCampaignId(long promoCampaignId) {
        this.promoCampaignId = promoCampaignId;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
}
