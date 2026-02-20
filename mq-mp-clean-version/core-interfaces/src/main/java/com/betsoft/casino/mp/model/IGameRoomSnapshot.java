package com.betsoft.casino.mp.model;

import com.dgphoenix.casino.common.cache.JsonSelfSerializable;
import com.esotericsoftware.kryo.KryoSerializable;

/**
 * User: flsh
 * Date: 25.09.18.
 */
public interface IGameRoomSnapshot<SEAT extends ISeat, MAP extends IMap, GRS extends IGameRoomSnapshot> 
        extends KryoSerializable, JsonSelfSerializable<GRS> {
    SEAT[] getSeats();

    MAP getMap();

    long getRoomId();

    long getRoundId();
}
