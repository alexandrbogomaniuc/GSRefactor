package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IMapConfigEntity;

public interface IMapConfigService {
    void save(int mapId, IMapConfigEntity config);

    void removeConfig(int mapId);

    IMapConfigEntity load(int mapId);
}
