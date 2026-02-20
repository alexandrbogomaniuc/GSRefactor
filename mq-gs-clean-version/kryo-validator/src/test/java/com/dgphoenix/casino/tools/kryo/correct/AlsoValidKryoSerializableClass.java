package com.dgphoenix.casino.tools.kryo.correct;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Created by isador
 * on 31.05.17
 */
public class AlsoValidKryoSerializableClass implements SubKryoSerializable {

    private int intValue;

    @Override
    public void someMethod() {
        // some stuff
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(intValue, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        intValue = input.readInt(true);
    }
}
