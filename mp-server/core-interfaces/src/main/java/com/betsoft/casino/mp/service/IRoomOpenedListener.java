package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.web.IGameSocketClient;

/**
 * User: flsh
 * Date: 14.07.2022.
 */
@SuppressWarnings("rawtypes")
public interface IRoomOpenedListener {
    void notifyRoomOpened(IRoom room, IGameSocketClient client);
}
