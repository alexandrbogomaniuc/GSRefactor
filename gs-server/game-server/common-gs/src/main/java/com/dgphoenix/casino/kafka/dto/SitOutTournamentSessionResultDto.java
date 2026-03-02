package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.TournamentInfoDto;

import com.abs.casino.kafka.dto.BasicKafkaResponse;

public class SitOutTournamentSessionResultDto extends BasicKafkaResponse {
    private TournamentInfoDto tournamentSession;
    private long activeFRBonusId;

    public SitOutTournamentSessionResultDto() {}

    public SitOutTournamentSessionResultDto(TournamentInfoDto tournamentSession,
            long activeFRBonusId,
            boolean success,
            int errorCode,
            String errorDetails) {
        super(success, errorCode, errorDetails);
        this.tournamentSession = tournamentSession;
        this.activeFRBonusId = activeFRBonusId;
    }

    public SitOutTournamentSessionResultDto(boolean success, int errorCode, String errorDetails) {
        super(success, errorCode, errorDetails);
    }

    public TournamentInfoDto getTournamentSession() {
        return tournamentSession;
    }

    public long getActiveFRBonusId() {
        return activeFRBonusId;
    }

    public void setTournamentSession(TournamentInfoDto tournamentSession) {
        this.tournamentSession = tournamentSession;
    }

    public void setActiveFRBonusId(long activeFRBonusId) {
        this.activeFRBonusId = activeFRBonusId;
    }
}
