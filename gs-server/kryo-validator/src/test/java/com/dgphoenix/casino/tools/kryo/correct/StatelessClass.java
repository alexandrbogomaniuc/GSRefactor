package com.abs.casino.tools.kryo.correct;

import com.abs.casino.tools.annotations.IgnoreValidation;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

@IgnoreValidation
public class StatelessClass implements KryoSerializable {
    private String stringField;
    private int intField;

    public StatelessClass() {}

    public StatelessClass(String stringField, int intField) {
        this.stringField = stringField;
        this.intField = intField;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        // nothing to serialize
    }

    @Override
    public void read(Kryo kryo, Input input) {
        // nothing to deserialize
    }
}
