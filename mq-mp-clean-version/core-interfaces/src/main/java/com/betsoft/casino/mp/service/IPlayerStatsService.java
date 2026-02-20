package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IPlayerStats;

public interface IPlayerStatsService<STAT extends IPlayerStats> {

    STAT addStats(long bankId, long gameId, long accountId, STAT stats);
    STAT addTournamentStats(long tournamentId, long bankId, long gameId, long accountId, STAT stats);

    STAT load(long bankId, long gameId, long accountId);
    STAT loadTournamentStats(long tournamentId, long bankId, long gameId, long accountId);
}
