package com.dgphoenix.casino.cassandra.persist;

import com.dgphoenix.casino.common.cache.data.bet.ShortBetInfo;

public interface IShortBetInfoProcessor {
    void process(ShortBetInfo betInfo) throws Exception;
}
