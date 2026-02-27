package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.cache.JsonSelfSerializable;
import com.abs.casino.gs.managers.game.event.IGameEvent;
import com.esotericsoftware.kryo.KryoSerializable;

/**
 * User: flsh
 * Date: 15.11.16.
 */
public interface IParticipantEvent<T extends IParticipantEvent> extends IGameEvent, KryoSerializable, JsonSelfSerializable<T> {
    SignificantEventType getType();

    long getGameId();

    long getEventDate();

    long getAccountId();

    String getAccountExternalId();
}
