package com.abs.casino.common.promo.feed.tournament;

import com.abs.casino.common.promo.TournamentMemberRank;

public interface IRecordProducer {

    ITournamentFeedRecord produce(String place, TournamentMemberRank rank);
}
