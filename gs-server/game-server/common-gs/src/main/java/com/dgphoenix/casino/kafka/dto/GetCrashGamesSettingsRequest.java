package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.KafkaRequest;

import java.util.Set;

public class GetCrashGamesSettingsRequest implements KafkaRequest {
    private Set<Long> bankIds;
    private int gameId;

    public GetCrashGamesSettingsRequest() {}

    public GetCrashGamesSettingsRequest(Set<Long> bankIds, int gameId) {
        this.bankIds = bankIds;
        this.gameId = gameId;
    }

    public Set<Long> getBankIds() {
        return bankIds;
    }

    public int getGameId() {
        return gameId;
    }

    public void setBankIds(Set<Long> bankIds) {
        this.bankIds = bankIds;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }
}
