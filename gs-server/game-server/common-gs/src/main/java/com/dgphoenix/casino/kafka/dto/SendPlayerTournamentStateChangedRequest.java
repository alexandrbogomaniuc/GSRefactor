package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class SendPlayerTournamentStateChangedRequest implements KafkaRequest {
    private String sessionId;
    private long tournamentId;
    private boolean cannotJoin;
    private boolean joined;

    public SendPlayerTournamentStateChangedRequest() {}

    public SendPlayerTournamentStateChangedRequest(String sessionId,
            long tournamentId,
            boolean cannotJoin,
            boolean joined) {
        this.sessionId = sessionId;
        this.tournamentId = tournamentId;
        this.cannotJoin = cannotJoin;
        this.joined = joined;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public boolean isCannotJoin() {
        return cannotJoin;
    }

    public boolean isJoined() {
        return joined;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public void setCannotJoin(boolean cannotJoin) {
        this.cannotJoin = cannotJoin;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }
}
