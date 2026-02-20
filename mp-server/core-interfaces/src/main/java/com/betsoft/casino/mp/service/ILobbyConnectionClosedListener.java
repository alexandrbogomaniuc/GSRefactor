package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.ILobbySocketClient;

/**
 * User: flsh
 * Date: 14.07.2022.
 */
@SuppressWarnings("rawtypes")
public interface ILobbyConnectionClosedListener {
    void notifyLobbyConnectionClosed(ILobbySocketClient client);
}
