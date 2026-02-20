package com.dgphoenix.casino.common.mp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

public class XPAwardLevel implements KryoSerializable, Serializable {
    private static final byte VERSION = 0;

    private int fromPlace;
    private int toPlace;
    private int amount;

    public XPAwardLevel() {}

    public XPAwardLevel(int fromPlace, int toPlace, int amount) {
        this.fromPlace = fromPlace;
        this.toPlace = toPlace;
        this.amount = amount;
    }

    public int getFromPlace() {
        return fromPlace;
    }

    public int getToPlace() {
        return toPlace;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "XPAwardLevel{" +
                "fromPlace=" + fromPlace +
                ", toPlace=" + toPlace +
                ", amount=" + amount +
                '}';
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(fromPlace, true);
        output.writeInt(toPlace, true);
        output.writeInt(amount, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        fromPlace = input.readInt(true);
        toPlace = input.readInt(true);
        amount = input.readInt(true);
    }
}
