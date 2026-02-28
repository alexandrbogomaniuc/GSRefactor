package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;
import com.abs.casino.kafka.dto.PromoNotificationType;

import java.util.Set;

public class SendPromoNotificationsRequest implements KafkaRequest {
    private String sessionId;
    private long campaignId;
    private Set<PromoNotificationType> notificationsTypes;

    public SendPromoNotificationsRequest() {}

    public SendPromoNotificationsRequest(String sessionId,
            long campaignId,
            Set<PromoNotificationType> notificationsTypes) {
        this.sessionId = sessionId;
        this.campaignId = campaignId;
        this.notificationsTypes = notificationsTypes;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getCampaignId() {
        return campaignId;
    }

    public Set<PromoNotificationType> getNotificationsTypes() {
        return notificationsTypes;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setCampaignId(long campaignId) {
        this.campaignId = campaignId;
    }

    public void setNotificationsTypes(Set<PromoNotificationType> notificationsTypes) {
        this.notificationsTypes = notificationsTypes;
    }
}
