package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.KafkaRequest;

public class GetDetailedPlayerInfo2Request implements KafkaRequest {
    private String sessionId;
    private long gameId;
    private String mode;
    private long bonusId;
    private long tournamentId;

    public GetDetailedPlayerInfo2Request() {}

    public GetDetailedPlayerInfo2Request(String sessionId,
                                         long gameId,
                                         String mode,
                                         long bonusId,
                                         long tournamentId) {
        this.setSessionId(sessionId);
        this.setGameId(gameId);
        this.setMode(mode);
        this.setBonusId(bonusId);
        this.setTournamentId(tournamentId);
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public long getBonusId() {
        return bonusId;
    }

    public void setBonusId(long bonusId) {
        this.bonusId = bonusId;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

}
