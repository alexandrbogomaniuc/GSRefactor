package com.abs.casino.kafka.dto.privateroom.request;

import com.abs.casino.kafka.dto.KafkaRequest;


public class GetParticipantAccountIdsInRoundDto implements KafkaRequest {
    private long accountId;
    private long gameSessionId;

    public GetParticipantAccountIdsInRoundDto(){}

    public GetParticipantAccountIdsInRoundDto(long accountId, long gameSessionId) {
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
