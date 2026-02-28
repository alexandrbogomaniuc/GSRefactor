package com.abs.casino.kafka.dto.privateroom.request;

import com.abs.casino.kafka.dto.KafkaRequest;

public class PrivateRoomIdDto implements KafkaRequest {
    private String ownerUsername;
    private int gameId;
    private long bankId;
    private long buyIn;
    private String currency;
    private long ownerAccountId;
    private String ownerExternalId;

    public PrivateRoomIdDto(){}

    public PrivateRoomIdDto(String ownerUsername, int gameId, long bankId, long buyIn, String currency, long ownerAccountId, String ownerExternalId) {
        this.ownerUsername = ownerUsername;
        this.gameId = gameId;
        this.bankId = bankId;
        this.buyIn = buyIn;
        this.currency = currency;
        this.ownerAccountId = ownerAccountId;
        this.ownerExternalId = ownerExternalId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public long getBuyIn() {
        return buyIn;
    }

    public void setBuyIn(long buyIn) {
        this.buyIn = buyIn;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getOwnerAccountId() {
        return ownerAccountId;
    }

    public void setOwnerAccountId(long ownerAccountId) {
        this.ownerAccountId = ownerAccountId;
    }

    public String getOwnerExternalId() {
        return ownerExternalId;
    }

    public void setOwnerExternalId(String ownerExternalId) {
        this.ownerExternalId = ownerExternalId;
    }
}
