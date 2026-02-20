package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.room.IRoom;

/**
 * User: flsh
 * Date: 14.07.2022.
 */
@SuppressWarnings("rawtypes")
public interface ISeatsCountChangedListener {
    void notifySeatAdded(IRoom room, ISeat<?, ?, ?, ?, ?> seat);

    void notifySeatRemoved(IRoom room, ISeat<?, ?, ?, ?, ?> seat);
}
