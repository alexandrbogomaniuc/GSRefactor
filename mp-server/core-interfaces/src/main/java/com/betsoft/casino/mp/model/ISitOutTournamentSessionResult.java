package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 18.08.2020.
 */
public interface ISitOutTournamentSessionResult extends ISitOutResult {
    ITournamentSession getTournamentSession();

    Long getActiveFRBonusId();
}
