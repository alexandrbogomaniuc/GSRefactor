package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IGameConfigEntity;
import com.betsoft.casino.mp.model.gameconfig.IGameConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameConfigProvider implements IGameConfigProvider {
    private final IGameConfigService<? extends IGameConfigEntity> configService;

    private final Map<Long, IGameConfig> defaultConfigs = new ConcurrentHashMap<>();
    private final Map<Long, IGameConfig> roomConfigs = new ConcurrentHashMap<>();

    public GameConfigProvider(IGameConfigService<? extends IGameConfigEntity> configService) {
        this.configService = configService;
    }

    @Override
    public void registerDefaultConfig(long gameId, IGameConfig gameConfig) {
        this.defaultConfigs.put(gameId, gameConfig);
    }

    @Override
    public IGameConfig getConfig(long gameId, long roomId) {
        IGameConfig config = roomConfigs.get(roomId);
        if (config == null) {
            IGameConfigEntity entity = configService.load(roomId);
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
