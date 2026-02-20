package com.betsoft.casino.mp.service;

/**
 * User: flsh
 * Date: 28.10.18.
 */
public interface IGameServerStatusListener {
    void notify(int gameServerId, boolean online) throws InterruptedException;
}
