package com.dgphoenix.casino.tools.kryo.correct;

import com.abs.casino.tools.annotations.Preset;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 7/14/22
 */
public class PredefinedEntity implements KryoSerializable {

    @Preset("predefined")
    private String predefinedStringField;
    @Preset("two")
    private SimpleEnum predefinedEnum;

    public PredefinedEntity() {}

    public PredefinedEntity(@Preset("two") SimpleEnum predefinedEnum) {
        if (predefinedEnum != SimpleEnum.TWO) {
            throw new IllegalStateException("Wrong value: " + predefinedEnum);
        }
        this.predefinedEnum = predefinedEnum;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeString(predefinedStringField);
        output.writeInt(predefinedEnum.ordinal(), true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        predefinedStringField = input.readString();
        assertEquals("predefined", predefinedStringField);
        predefinedEnum = SimpleEnum.values()[input.readInt(true)];
        assertEquals(SimpleEnum.TWO, predefinedEnum);
    }
}
