package com.dgphoenix.casino.common.promo.ai;

import java.util.Map;

public interface ITournamentFeedHistoryPersister {

    void persistRecords(long tournamentId, int time, Map<String, Long> scores);
    Map<String, Long> getRecords(long tournamentId, int time);
}

