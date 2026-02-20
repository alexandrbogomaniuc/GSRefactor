package com.betsoft.casino.mp.model;

import com.dgphoenix.casino.common.cache.Identifiable;
import com.dgphoenix.casino.common.mp.LeaderboardStatus;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface ILeaderboard extends Identifiable {
    long getStart();

    long getEnd();

    int getAwards();

    LeaderboardStatus getStatus();
}
