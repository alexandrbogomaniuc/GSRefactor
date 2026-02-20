package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.room.IRoomInfo;

/**
 * User: flsh
 * Date: 11.11.17.
 */
public interface IRoomSubscriptionCallback {
    String getSessionId();

    void notifySeatsChanged(IRoomInfo room);

    long getSubscriptionId();

    default void notifyStateChanged(IRoomInfo room) {
        notifySeatsChanged(room);
    }
}
