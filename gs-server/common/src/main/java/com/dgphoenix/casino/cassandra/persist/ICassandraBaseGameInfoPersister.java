package com.dgphoenix.casino.cassandra.persist;

import com.abs.casino.common.cache.data.currency.ICurrency;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfo;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by mic on 23.12.14.
 */
public interface ICassandraBaseGameInfoPersister<BGI extends IBaseGameInfo> {
    void persist(BaseGameInfo gameInfo);

    BGI get(String key);

    boolean delete(String key);

    List<BGI> getByBank(long bankId);

    List<BGI> getByBankAndCurrency(long bankId, ICurrency currency);

    Set<String> getKeys();

    void persist(String key, BGI gameInfo);

    Map<String, BGI> getAllAsMap();
}
