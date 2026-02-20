package com.dgphoenix.casino.tools.kryo.correct;


import com.dgphoenix.casino.tools.annotations.Preset;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 13.10.2015
 */
public class SimpleEntity implements KryoSerializable {
    private static final byte VERSION = 0;

    @PositiveOrZero
    @Max(35)
    private int primitiveIntegerField;
    private long primitiveLongField;
    private byte primitiveByteField;
    private short primitiveShortField;
    private boolean primitiveBooleanField;
    private double primitiveDoubleField;
    private float primitiveFloatField;

    private String stringField;
    @Negative
    @Min(-10)
    private Integer integerField;
    private Long longField;
    private Byte byteField;
    private Short shortField;
    private Boolean booleanField;
    private Double doubleField;
    private Float floatField;
    private Date dateField;
    private SimpleEnum enumField;
    private BigDecimal bigDecimalField;
    @Preset("predefined")
    private String predefinedStringField;

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(primitiveIntegerField, true);
        output.writeLong(primitiveLongField, true);
        output.writeByte(primitiveByteField);
        output.writeShort(primitiveShortField);
        output.writeBoolean(primitiveBooleanField);
        output.writeFloat(primitiveFloatField);
        output.writeDouble(primitiveDoubleField);
        kryo.writeObjectOrNull(output, stringField, String.class);
        kryo.writeObjectOrNull(output, integerField, Integer.class);
        kryo.writeObjectOrNull(output, longField, Long.class);
        kryo.writeObjectOrNull(output, byteField, Byte.class);
        kryo.writeObjectOrNull(output, shortField, Short.class);
        kryo.writeObjectOrNull(output, booleanField, Boolean.class);
        kryo.writeObjectOrNull(output, doubleField, Double.class);
        kryo.writeObjectOrNull(output, floatField, Float.class);
        kryo.writeObjectOrNull(output, dateField, Date.class);
        kryo.writeClassAndObject(output, enumField);
        kryo.writeObjectOrNull(output, bigDecimalField, BigDecimal.class);
        output.writeString(predefinedStringField);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        primitiveIntegerField = input.readInt(true);
        assertTrue("Field value should be less or equals than 35, but was " + primitiveIntegerField, primitiveIntegerField <= 35);
        assertTrue("Field value should be greater or equals than 0, but was " + primitiveIntegerField, primitiveIntegerField >= 0);
        primitiveLongField = input.readLong(true);
        primitiveByteField = input.readByte();
        primitiveShortField = input.readShort();
        primitiveBooleanField = input.readBoolean();
        primitiveFloatField = input.readFloat();
        primitiveDoubleField = input.readDouble();
        stringField = kryo.readObjectOrNull(input, String.class);
        integerField = kryo.readObjectOrNull(input, Integer.class);
        assertTrue("Field value should be less than zero, but was" + integerField, integerField < 0);
        assertTrue("Field value should be greater or equals than -10, but was" + integerField, integerField >= -10);
        longField = kryo.readObjectOrNull(input, Long.class);
        byteField = kryo.readObjectOrNull(input, Byte.class);
        shortField = kryo.readObjectOrNull(input, Short.class);
        booleanField = kryo.readObjectOrNull(input, Boolean.class);
        doubleField = kryo.readObjectOrNull(input, Double.class);
        floatField = kryo.readObjectOrNull(input, Float.class);
        dateField = kryo.readObjectOrNull(input, Date.class);
        enumField = (SimpleEnum) kryo.readClassAndObject(input);
        bigDecimalField = kryo.readObjectOrNull(input, BigDecimal.class);
        predefinedStringField = input.readString();
        assertEquals("predefined", predefinedStringField);
    }

}
