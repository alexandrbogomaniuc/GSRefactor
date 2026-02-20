package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.gameconfig.ISpawnConfig;

public interface ISpawnConfigProvider {
    void registerDefaultConfig(long gameId, ISpawnConfig gameConfig);

    ISpawnConfig getConfig(long gameId, long roomId);

    void removeCachedConfig(long roomId);
}
