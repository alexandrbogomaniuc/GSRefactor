package com.abs.casino.kafka.dto;

public class GetMQDataRequest implements KafkaRequest {
    private long accountId;
    private long gameId;

    public GetMQDataRequest() {}

    public GetMQDataRequest(long accountId, long gameId) {
        this.accountId = accountId;
        this.gameId = gameId;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }
}
