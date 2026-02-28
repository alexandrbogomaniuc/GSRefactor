package com.abs.casino.kafka.dto;

public class SitOutRequest2 implements KafkaRequest {
    private long accountId;
    private long gameSessionId;

    public SitOutRequest2() {}

    public SitOutRequest2(long accountId, long gameSessionId) {
        this.accountId = accountId;
        this.gameSessionId = gameSessionId;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }
}
