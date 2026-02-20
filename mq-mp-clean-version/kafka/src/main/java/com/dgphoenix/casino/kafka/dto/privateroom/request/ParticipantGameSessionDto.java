package com.dgphoenix.casino.kafka.dto.privateroom.request;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;


public class ParticipantGameSessionDto implements KafkaRequest {
    private long accountId;
    private long gameSessionId;

    public ParticipantGameSessionDto(){}

    public ParticipantGameSessionDto(long accountId, long gameSessionId) {
        this.accountId = accountId;
        this.gameSessionId = gameSessionId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }
}
