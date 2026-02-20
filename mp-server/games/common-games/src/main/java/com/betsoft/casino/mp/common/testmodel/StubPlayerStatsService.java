package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.IPlayerStats;
import com.betsoft.casino.mp.service.IPlayerStatsService;

/**
 * User: flsh
 * Date: 20.02.2020.
 */
public class StubPlayerStatsService implements IPlayerStatsService {
    @Override
    public IPlayerStats addStats(long bankId, long gameId, long accountId, IPlayerStats stats) {
        return new StubPlayerStats();
    }

    @Override
    public IPlayerStats addTournamentStats(long tournamentId, long bankId, long gameId, long accountId, IPlayerStats stats) {
        return new StubPlayerStats();
    }

    @Override
    public IPlayerStats loadTournamentStats(long tournamentId, long bankId, long gameId, long accountId) {
        return new StubPlayerStats();
    }

    @Override
    public IPlayerStats load(long bankId, long gameId, long accountId) {
        return new StubPlayerStats();
    }

}
