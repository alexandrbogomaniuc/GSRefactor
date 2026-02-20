package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.gameconfig.IMapConfig;

public interface IMapConfigEntity {
    String getUploadDate();

    void setUploadDate(String uploadDate);

    IMapConfig getConfig();

    void setConfig(IMapConfig config);
}
