package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.gameconfig.ISpawnConfig;
import com.betsoft.casino.mp.service.ISpawnConfigProvider;

public class StubSpawnConfigProvider implements ISpawnConfigProvider {
    private final ISpawnConfig spawnConfig;

    public StubSpawnConfigProvider(ISpawnConfig spawnConfig) {
        this.spawnConfig = spawnConfig;
    }

    @Override
    public void registerDefaultConfig(long gameId, ISpawnConfig gameConfig) {

    }

    @Override
    public ISpawnConfig getConfig(long gameId, long roomId) {
        return spawnConfig;
    }

    @Override
    public void removeCachedConfig(long roomId) {

    }
}
