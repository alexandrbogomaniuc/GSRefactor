package com.abs.casino.common.cache;

import com.abs.casino.common.cache.data.bank.Coin;

import java.util.List;

@Deprecated
public class CoinsCache {
    private static final CoinsCache instance = new CoinsCache();

    private CoinsCache() {
    }

    public static CoinsCache getInstance() {
        return instance;
    }

    @Deprecated
    public Coin getCoin(long id) {
        return Coin.getById(id);
    }

    @Deprecated
    public Coin getCoinByValue(long coinValue) {
        return Coin.getByValue(coinValue);
    }

    @Deprecated
    public List<Coin> getCoinsByValues(long... coinValues) {
        return Coin.getByValues(coinValues);
    }

    @Deprecated
    public List<Coin> getAll() {
        return Coin.getAll();
    }
}