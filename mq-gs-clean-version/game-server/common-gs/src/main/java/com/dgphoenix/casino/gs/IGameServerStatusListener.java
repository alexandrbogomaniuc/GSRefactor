package com.dgphoenix.casino.gs;

/**
 * User: flsh
 * Date: 20.02.15.
 */
public interface IGameServerStatusListener {
    void notify(int gameServerId, boolean online);
}
