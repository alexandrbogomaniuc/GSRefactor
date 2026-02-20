package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.ISpawnConfigEntity;

public interface ISpawnConfigService<CONFIG extends ISpawnConfigEntity> {
    void save(long roomId, CONFIG gameConfig);

    void removeConfig(long roomId);

    CONFIG load(long roomId);
}
