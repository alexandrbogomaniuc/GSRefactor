package com.dgphoenix.casino.promo.persisters;

import com.dgphoenix.casino.common.promo.feed.tournament.ISummaryTournamentFeedEntry;

import java.util.List;

public interface ISummaryFeedTransformer {
    List<ISummaryTournamentFeedEntry> transform(long tournamentId, List<ISummaryTournamentFeedEntry> source);
}
