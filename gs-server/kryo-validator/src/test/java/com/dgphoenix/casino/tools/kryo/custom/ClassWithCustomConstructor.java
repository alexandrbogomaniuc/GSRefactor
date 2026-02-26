package com.abs.casino.tools.kryo.custom;

import com.abs.casino.tools.annotations.Transient;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import javax.validation.constraints.Positive;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 14.02.18
 */
public class ClassWithCustomConstructor implements KryoSerializable {

    private long bet;
    private long win;
    @Transient
    private String stringField;
    @Transient
    private byte byteField;
    @Transient
    private short shortField;
    @Transient
    private int integerField;
    @Transient
    private long longField;
    @Transient
    private float floatField;
    @Transient
    private double doubleField;
    @Transient
    private boolean booleanField;

    public ClassWithCustomConstructor() {}

    public ClassWithCustomConstructor(long bet, @Positive long win, String stringField, byte byteField, short shortField, int integerField,
                                      long longField, float floatField, double doubleField, boolean booleanField) {
        this.bet = bet;
        checkArgument(win > 0, "Win should be greater than 0");
        this.win = win;
        this.stringField = stringField;
        this.byteField = byteField;
        this.shortField = shortField;
        this.integerField = integerField;
        this.longField = longField;
        this.floatField = floatField;
        this.doubleField = doubleField;
        this.booleanField = booleanField;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeLong(bet);
        output.writeLong(win, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        bet = input.readLong();
        win = input.readLong(true);
    }
}
