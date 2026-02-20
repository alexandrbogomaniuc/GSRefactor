package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.gameconfig.IGameConfig;

/**
 * User: flsh
 * Date: 15.05.2020.
 */
public interface IGameConfigEntity {
    String getUploadDate();

    void setUploadDate(String uploadDate);

    String getConfigName();

    void setConfigName(String configName);

    IGameConfig getConfig();

    void setConfig(IGameConfig config);

    int getVersion();

    void setVersion(int version);
}
