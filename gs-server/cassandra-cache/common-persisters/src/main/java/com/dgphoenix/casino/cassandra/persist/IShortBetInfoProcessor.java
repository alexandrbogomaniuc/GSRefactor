package com.abs.casino.cassandra.persist;

import com.abs.casino.common.cache.data.bet.ShortBetInfo;

public interface IShortBetInfoProcessor {
    void process(ShortBetInfo betInfo) throws Exception;
}
