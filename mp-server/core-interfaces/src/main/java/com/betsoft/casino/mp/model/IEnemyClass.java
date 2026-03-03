package com.betsoft.casino.mp.model;

import com.abs.casino.common.cache.Identifiable;
import com.abs.casino.common.cache.JsonSelfSerializable;
import com.esotericsoftware.kryo.KryoSerializable;

/**
 * User: flsh
 * Date: 08.02.19.
 */
public interface IEnemyClass<EC extends IEnemyClass> extends Identifiable, 
        KryoSerializable, JsonSelfSerializable<EC> {
    short getWidth();

    short getHeight();

    String getName();

    double getEnergy();

    float getSpeed();

    IEnemyType getEnemyType();
}
