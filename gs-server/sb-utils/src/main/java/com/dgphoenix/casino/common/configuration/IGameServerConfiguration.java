package com.abs.casino.common.configuration;

/**
 * User: flsh
 * Date: 23.09.2019.
 */
public interface IGameServerConfiguration {
    String getStringPropertySilent(String propertyName);

    CasinoSystemType getCasinoSystemType();

    String getDomain();

    String getGsDomain();

    long getGameServerId();

    long getSystemId();

    int getClusterId();

    String getHost();

    String getServerLabel();

    boolean isStressTestMode();

    boolean isLive();

    boolean isTestSystem();

    String getClusterName();

    boolean isIpTrusted(String ip);

    boolean isCountryTrusted(String ip);
}
