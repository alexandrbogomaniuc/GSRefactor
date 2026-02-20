package com.dgphoenix.casino.promo;

import com.dgphoenix.casino.common.promo.TournamentMemberRanks;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItem;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItemType;
import com.dgphoenix.casino.common.transactiondata.storeddate.identifier.StoredItemInfo;

public final class TournamentRanksExtractor {
    private TournamentRanksExtractor() {

    }

    public static TournamentMemberRanks extractRanksFromTD(ITransactionData transactionData) {
        StoredItem<TournamentMemberRanks, StoredItemInfo<TournamentMemberRanks>> ranksItem =
                transactionData.get(StoredItemType.PROMO_TOURNAMENT_RANKS);
        TournamentMemberRanks ranks;
        if (ranksItem != null) {
            ranks = ranksItem.getItem();
        } else {
            ranks = new TournamentMemberRanks();
            transactionData.add(StoredItemType.PROMO_TOURNAMENT_RANKS, ranks, null);
        }
        return ranks;
    }
}
