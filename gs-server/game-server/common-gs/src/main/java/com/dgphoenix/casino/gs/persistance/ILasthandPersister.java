package com.abs.casino.gs.persistance;

import com.abs.casino.common.cache.data.account.LasthandInfo;

public interface ILasthandPersister {
    LasthandInfo get(long accountId, long gameId);
}
