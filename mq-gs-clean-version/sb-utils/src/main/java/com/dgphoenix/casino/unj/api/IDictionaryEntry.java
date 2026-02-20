package com.dgphoenix.casino.unj.api;

import com.esotericsoftware.kryo.KryoSerializable;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 29.03.13
 */
public interface IDictionaryEntry extends Serializable, KryoSerializable {
    String getKey();
    String getDictionaryId();
}
