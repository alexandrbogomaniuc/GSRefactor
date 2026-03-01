package com.abs.casino.common;

/**
 * User: flsh
 * Date: 25.03.15.
 */
public interface ILoadBalancer {
    Long getServerUpdateTime(int serverId);

    Long getStartTime(int serverId);

    boolean isOnline(int serverId);
}
