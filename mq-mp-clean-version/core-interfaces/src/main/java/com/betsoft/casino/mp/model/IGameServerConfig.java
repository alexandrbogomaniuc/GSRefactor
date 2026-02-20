package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IGameServerConfig {
    int getId();

    void setId(int id);

    String getHost();

    void setHost(String host);

    String getDomain();

    void setDomain(String domain);

    boolean isOnline();

    void setOnline(boolean online);
}
