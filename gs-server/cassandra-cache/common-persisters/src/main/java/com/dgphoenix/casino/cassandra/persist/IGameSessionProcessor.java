package com.abs.casino.cassandra.persist;

import com.dgphoenix.casino.common.cache.data.session.GameSession;

/**
 * Created by quant on 07.04.17.
 */
public interface IGameSessionProcessor {
    void process(GameSession gameSession);
}
