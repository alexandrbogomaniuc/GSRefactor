package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 16.07.2020.
 */
public class SitOutTournamentSessionResult extends SitOutResult implements ISitOutTournamentSessionResult {
    private TournamentSession tournamentSession;
    private Long activeFRBonusId;

    public SitOutTournamentSessionResult(boolean success, int errorCode, String errorDetails,
                                         TournamentSession tournamentSession, Long activeFRBonusId) {
        super(success, errorCode, errorDetails);
        this.tournamentSession = tournamentSession;
        this.activeFRBonusId = activeFRBonusId;
    }

    @Override
    public ITournamentSession getTournamentSession() {
        return tournamentSession;
    }

    @Override
    public Long getActiveFRBonusId() {
        return activeFRBonusId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SitOutTournamentSessionResult [");
        sb.append("tournamentSession=").append(tournamentSession);
        sb.append(", success=").append(success);
        sb.append(", activeFRBonusId=").append(activeFRBonusId);
        sb.append(", errorCode=").append(errorCode);
        sb.append(", errorDetails='").append(errorDetails).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
