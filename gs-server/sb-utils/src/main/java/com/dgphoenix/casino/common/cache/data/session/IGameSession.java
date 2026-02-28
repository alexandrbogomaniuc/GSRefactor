package com.abs.casino.common.cache.data.session;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.dgphoenix.casino.common.cache.Identifiable;

/**
 * User: flsh
 * Date: 14.04.17.
 */
public interface IGameSession extends IDistributedCacheEntry, Identifiable {
    long getId();

    long getAccountId();

    long getBetsCount();

    boolean isCreateNewBet();

    Long getEndTime();

    long getGameId();

    long getIncome();

    long getPayout();

    boolean isRealMoney();

    long getRoundsCount();

    long getStartTime();
}
