package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.gameconfig.IGameConfig;
import com.betsoft.casino.mp.service.IGameConfigProvider;

public class StubGameConfigProvider implements IGameConfigProvider {
    private final IGameConfig config;

    public StubGameConfigProvider(IGameConfig config) {
        this.config = config;
    }

    @Override
    public void registerDefaultConfig(long gameId, IGameConfig gameConfig) {

    }

    @Override
    public IGameConfig getConfig(long gameId, long roomId) {
        return config;
    }

    @Override
    public void removeCachedConfig(long roomId) {

    }
}
