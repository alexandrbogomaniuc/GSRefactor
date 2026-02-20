package com.dgphoenix.casino.common.promo;

import java.util.HashMap;
import java.util.Map;

public class TournamentMemberRanks {
    private final Map<Long, TournamentMemberRank> ranksByCampaignId = new HashMap<Long, TournamentMemberRank>();

    public void addRank(TournamentMemberRank rank) {
        ranksByCampaignId.put(rank.getCampaignId(), rank);
    }

    public Iterable<TournamentMemberRank> getRanks() {
        return ranksByCampaignId.values();
    }
}
