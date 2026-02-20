package com.betsoft.casino.mp.model;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

public class Experience implements IExperience, KryoSerializable, Serializable {
    private double amount;

    public Experience() {}

    public Experience(double amount) {
        this.amount = amount;
    }

    @Override
    public void add(double xp) {
        this.amount += xp;
    }

    @Override
    public void add(IExperience xp) {
        this.amount += xp.getAmount();
    }

    @Override
    public void multiply(double multiplier) {
        this.amount *= multiplier;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public long getLongAmount() {
        return (long) Math.floor(amount);
    }

    @Override
    public long getDiff(IExperience other) {
        return getLongAmount() - other.getLongAmount();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeDouble(amount);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        amount = input.readDouble();
    }

    @Override
    public String toString() {
        return "Experience[" +
                "amount=" + amount +
                ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Experience that = (Experience) o;

        return Double.compare(that.amount, amount) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(amount);
        return (int) (temp ^ (temp >>> 32));
    }
}
