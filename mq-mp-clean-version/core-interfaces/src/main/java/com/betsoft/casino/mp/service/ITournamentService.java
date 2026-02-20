package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.ITournamentSession;

import java.util.List;

public interface ITournamentService {

    ITournamentSession get(long tournamentId, long accountId);
    List<ITournamentSession> getByTournament(long tournamentId);
    void persist(ITournamentSession session);

}
