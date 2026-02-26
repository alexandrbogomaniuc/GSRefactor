package com.abs.casino.tools.kryo.custom;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Created by isador
 * on 9/21/17
 */
public class ClassWithCustomClass implements KryoSerializable {

    private CustomClass field;

    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeObjectOrNull(output, field, CustomClass.class);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        field = kryo.readObjectOrNull(input, CustomClass.class);
    }
}
