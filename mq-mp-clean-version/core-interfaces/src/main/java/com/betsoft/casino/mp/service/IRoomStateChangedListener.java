package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.mp.model.room.IRoom;

/**
 * User: flsh
 * Date: 14.07.2022.
 */
@SuppressWarnings("rawtypes")
public interface IRoomStateChangedListener {
    void notifyStateChanged(IRoom room, RoomState oldState, RoomState newState);
}
