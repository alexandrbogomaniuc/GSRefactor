package com.betsoft.casino.mp.model;

import com.esotericsoftware.kryo.KryoSerializable;

/**
 * User: flsh
 * Date: 26.02.2022.
 */
public interface ISharedGameState extends KryoSerializable {
    RoomState getState();

    long getRoomId();

    long getRoundId();

    long getRoundStartTime();

    long getRoundEndTime();
}
