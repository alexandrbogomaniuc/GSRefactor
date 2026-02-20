package com.dgphoenix.casino.tools.kryo.correct;

import com.dgphoenix.casino.tools.annotations.Transient;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Created by isador
 * on 31.05.17
 */
public class SubClass extends RootClass implements KryoSerializable {

    private int intValue;
    @Transient
    private Integer childTransient;

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeInt(intValue);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        intValue = input.readInt();
    }
}
