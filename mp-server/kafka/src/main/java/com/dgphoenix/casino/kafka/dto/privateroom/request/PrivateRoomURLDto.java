package com.abs.casino.kafka.dto.privateroom.request;

import com.abs.casino.kafka.dto.KafkaRequest;

public class PrivateRoomURLDto implements KafkaRequest {
    private String ownerUsername;
    private int gameId;
    private long bankId;
    private long buyIn;
    private String currency;
    private String domainUrl;
    private String ownerExternalId;

    public PrivateRoomURLDto(){}

    public PrivateRoomURLDto(String ownerUsername, int gameId, long bankId, long buyIn, String currency, String domainUrl, String ownerExternalId) {
        this.ownerUsername = ownerUsername;
        this.gameId = gameId;
        this.bankId = bankId;
        this.buyIn = buyIn;
        this.currency = currency;
        this.domainUrl = domainUrl;
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

    public String getDomainUrl() {
        return domainUrl;
    }

    public void setDomainUrl(String domainUrl) {
        this.domainUrl = domainUrl;
    }

    public String getOwnerExternalId() {
        return ownerExternalId;
    }

    public void setOwnerExternalId(String ownerExternalId) {
        this.ownerExternalId = ownerExternalId;
    }

}
