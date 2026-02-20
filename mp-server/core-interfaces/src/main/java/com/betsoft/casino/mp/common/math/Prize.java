package com.betsoft.casino.mp.common.math;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.Objects;

public class Prize implements KryoSerializable, Serializable {
    private static final byte VERSION = 0;
    private double minPayout;
    private double maxPayout;

    public Prize() {}

    public Prize(double minPayout, double maxPayout) {
        this.minPayout = minPayout;
        this.maxPayout = maxPayout;
    }

    public double getMinPayout() {
        return minPayout;
    }

    public void setMinPayout(double minPayout) {
        this.minPayout = minPayout;
    }

    public double getMaxPayout() {
        return maxPayout;
    }

    public void setMaxPayout(double maxPayout) {
        this.maxPayout = maxPayout;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeDouble(minPayout);
        output.writeDouble(maxPayout);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        minPayout = input.readDouble();
        maxPayout = input.readDouble();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prize that = (Prize) o;
        return minPayout == that.minPayout &&
                maxPayout == that.maxPayout;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minPayout, maxPayout);
    }

    @Override
    public String toString() {
        return "Prize{" +
                "minPayout=" + minPayout +
                ", maxPayout=" + maxPayout +
                '}';
    }
}
