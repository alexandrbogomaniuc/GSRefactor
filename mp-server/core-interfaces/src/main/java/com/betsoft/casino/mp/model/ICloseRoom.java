package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.ITransportObject;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface ICloseRoom extends ITransportObject {
    long getRoomId();

    void setRoomId(long roomId);
}
