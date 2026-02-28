package com.abs.casino.kafka.dto.privateroom.response;

import com.abs.casino.kafka.dto.BasicKafkaResponse;

public class PrivateRoomInfoResultDto extends BasicKafkaResponse {
    private String currency;
    private int gameId;
    private long bankId;
    private long buyIn;
    private int serverId;

    public PrivateRoomInfoResultDto(){}

    public PrivateRoomInfoResultDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public PrivateRoomInfoResultDto(String currency, int gameId, long bankId, long buyIn, int serverId) {
        this.currency = currency;
        this.gameId = gameId;
        this.bankId = bankId;
        this.buyIn = buyIn;
        this.serverId = serverId;
    }

    public PrivateRoomInfoResultDto(boolean success, int statusCode, String reasonPhrases, String currency, int gameId, long bankId, long buyIn, int serverId) {
        super(success, statusCode, reasonPhrases);
        this.currency = currency;
        this.gameId = gameId;
        this.bankId = bankId;
        this.buyIn = buyIn;
        this.serverId = serverId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }
}
