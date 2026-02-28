package com.abs.casino.kafka.dto;

public enum PromoNotificationType {
    CAMPAIGN_STATUS_CHANGED(0), PRIZE_WON(1), MISSION_OBJECTIVE(2), TOURNAMENT_INFO(3);

    private final int value;

    private PromoNotificationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
