package com.abs.casino.common.cache;

import com.dgphoenix.casino.common.cache.data.bank.Limit;

import java.util.List;

@Deprecated
public class LimitsCache {
    private static LimitsCache instance = new LimitsCache();

    private LimitsCache() {
    }

    public static LimitsCache getInstance() {
        return instance;
    }

    @Deprecated
    public Limit getLimit(long id) {
        return Limit.getById(id);
    }

    @Deprecated
    public List<Limit> getAll() {
        return Limit.getAllRegistered();
    }
}