package com.abs.casino.tools.kryo.correct;

import com.esotericsoftware.kryo.KryoSerializable;

/**
 * Created by isador
 * on 31.05.17
 */
public interface SubKryoSerializable extends KryoSerializable {
    void someMethod();
}
