package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class TournamentEndedDto implements KafkaRequest {
    private long tournamentId;
    private String oldStatus;
    private String newStatus;

    public TournamentEndedDto(){}

    public TournamentEndedDto(long tournamentId, String oldStatus, String newStatus) {
        this.tournamentId = tournamentId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
}
