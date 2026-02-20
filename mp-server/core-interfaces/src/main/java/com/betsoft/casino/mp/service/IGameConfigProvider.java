package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.gameconfig.IGameConfig;

public interface IGameConfigProvider {
    void registerDefaultConfig(long gameId, IGameConfig gameConfig);

    IGameConfig getConfig(long gameId, long roomId);

    void removeCachedConfig(long roomId);
}
