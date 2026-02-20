package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IGameConfigEntity;

public interface IGameConfigService<CONFIG extends IGameConfigEntity> {
    void save(long roomId, CONFIG gameConfig);

    void removeConfig(long roomId);

    CONFIG load(long roomId);
}
