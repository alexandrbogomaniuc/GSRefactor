package com.betsoft.casino.mp.model;

import com.esotericsoftware.kryo.KryoSerializable;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 15.05.2020.
 */
public interface IMember extends KryoSerializable, Serializable {
    int getId();

    void setId(int id);

    float getDeathDamage();

    void setDeathDamage(float deathDamage);

    float getScale();

    void setScale(float scale);

    int getSkin();

    void setSkin(int skin);

    String getMoveConfig();

    void setMoveConfig(String moveConfig);
}
