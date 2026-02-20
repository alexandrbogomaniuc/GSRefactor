package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.gameconfig.ISpawnConfig;

public interface ISpawnConfigEntity {
    String getUploadDate();

    void setUploadDate(String uploadDate);

    String getConfigName();

    void setConfigName(String configName);

    ISpawnConfig getConfig();

    void setConfig(ISpawnConfig config);

    int getVersion();

    void setVersion(int version);
}
