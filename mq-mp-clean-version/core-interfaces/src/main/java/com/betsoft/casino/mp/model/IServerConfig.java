package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IServerConfig {
    int getId();

    void setId(int id);

    String getLabel();

    void setLabel(String label);

    String getHost();

    void setOldHost(String host);

    String getDomain();

    void setDomain(String domain);

    boolean isOnline();

    void setOnline(boolean online);

    boolean isLocalDevAllowed();

    boolean isMaster();
}
