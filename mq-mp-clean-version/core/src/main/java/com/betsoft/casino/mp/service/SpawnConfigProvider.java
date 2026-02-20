package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.ISpawnConfigEntity;
import com.betsoft.casino.mp.model.gameconfig.ISpawnConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnConfigProvider implements ISpawnConfigProvider {
    private final ISpawnConfigService<? extends ISpawnConfigEntity> configService;

    private final Map<Long, ISpawnConfig> defaultConfigs = new ConcurrentHashMap<>();
    private final Map<Long, ISpawnConfig> roomConfigs = new ConcurrentHashMap<>();

    public SpawnConfigProvider(ISpawnConfigService<? extends ISpawnConfigEntity> configService) {
        this.configService = configService;
    }

    @Override
    public void registerDefaultConfig(long gameId, ISpawnConfig gameConfig) {
        this.defaultConfigs.put(gameId, gameConfig);
    }

    @Override
    public ISpawnConfig getConfig(long gameId, long roomId) {
        ISpawnConfig config = roomConfigs.get(roomId);
        if (config == null) {
            ISpawnConfigEntity entity = configService.load(roomId);
            if (entity != null) {
                config = entity.getConfig();
                roomConfigs.put(roomId, config);
            }
        }
        if (config == null) {
            config = defaultConfigs.get(gameId);
        }
        return config;
    }

    @Override
    public void removeCachedConfig(long roomId) {
        roomConfigs.remove(roomId);
    }
}
