package com.abs.casino.common.promo.ai;

import com.abs.casino.common.promo.IPromoCampaign;
import com.abs.casino.common.promo.TournamentMemberRank;
import com.abs.casino.common.promo.feed.tournament.IRecordProducer;
import com.abs.casino.common.promo.feed.tournament.TournamentFeed;
import com.google.common.collect.Multimap;

public interface IPromoFeedTransformer {
    TournamentFeed transform(ITournamentFeedHistoryPersister historyPersister,
                             IMQReservedNicknamePersister nicknamePersister,
                             IPromoCampaign campaign,
                             Multimap<String, TournamentMemberRank> feed,
                             IRecordProducer recordProducer,
                             long startWriteTime);
}
