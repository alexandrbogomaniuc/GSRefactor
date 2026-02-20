package com.dgphoenix.casino.gs.persistance;

import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;

public interface ILasthandPersister {
    LasthandInfo get(long accountId, long gameId);
}
