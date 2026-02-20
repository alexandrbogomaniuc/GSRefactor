package com.betsoft.casino.mp.service;

import java.util.Map;

import com.betsoft.casino.mp.model.IServerConfig;

/**
 * User: flsh
 * Date: 20.11.17.
 */
public interface IServerConfigService<CONFIG extends IServerConfig> {
    int getServerId();

    CONFIG getConfig();

    CONFIG getConfig(int id);

    Iterable<CONFIG> getConfigs();

    Map<Integer, CONFIG> getConfigsMap();

    void put(CONFIG config);

    boolean isThisIsAMaster();
}
