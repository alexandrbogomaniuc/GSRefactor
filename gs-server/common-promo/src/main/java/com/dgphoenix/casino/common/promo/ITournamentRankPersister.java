package com.abs.casino.common.promo;

import com.dgphoenix.casino.common.promo.*;
/**
 * Created by vladislav on 5/29/17.
 */
public interface ITournamentRankPersister {
    void persist(TournamentMemberRank rank);
    TournamentMemberRank getForAccount(long campaignId, long accountId);
}
