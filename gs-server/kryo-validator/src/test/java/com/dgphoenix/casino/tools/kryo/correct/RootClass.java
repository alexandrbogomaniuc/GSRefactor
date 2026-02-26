package com.abs.casino.tools.kryo.correct;

import com.abs.casino.tools.annotations.Transient;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Created by isador
 * on 31.05.17
 */
public class RootClass implements KryoSerializable {

    private String stringValue;
    @Transient
    private Integer parentTransient;

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeString(stringValue);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        stringValue = input.readString();
    }
}
