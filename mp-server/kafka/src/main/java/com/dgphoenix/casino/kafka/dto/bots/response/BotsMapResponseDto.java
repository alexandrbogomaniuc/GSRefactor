package com.abs.casino.kafka.dto.bots.response;

import com.betsoft.casino.mp.model.bots.dto.BotsMap;
import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;

public class BotsMapResponseDto extends BasicKafkaResponse {
    private BotsMap botsMap;

    public BotsMapResponseDto() {}

    public BotsMapResponseDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public BotsMapResponseDto(BotsMap botsMap) {
        super(true, 0, "");
        this.setBotsMap(botsMap);
    }

    public BotsMap getBotsMap() {
        return botsMap;
    }

    public void setBotsMap(BotsMap botsMap) {
        this.botsMap = botsMap;
    }
}
