package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.KafkaRequest;

public class InvalidateLocalBaseGameInfoRequest implements KafkaRequest {
    private long bankId;
    private long gameId;
    private String currencyCode;

    public InvalidateLocalBaseGameInfoRequest() {}

    public InvalidateLocalBaseGameInfoRequest(long bankId, long gameId, String currencyCode) {
        this.bankId = bankId;
        this.gameId = gameId;
        this.currencyCode = currencyCode;
    }

    public long getBankId() {
        return bankId;
    }

    public long getGameId() {
        return gameId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
