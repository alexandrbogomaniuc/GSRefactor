package com.betsoft.casino.mp.common.math;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.List;

public class LootboxPrizes implements KryoSerializable, Serializable {
    private int number;
    private List<LootboxPrize> prizes;

    public LootboxPrizes() {}

    public LootboxPrizes(int number, List<LootboxPrize> prizes) {
        this.number = number;
        this.prizes = prizes;
    }

    public int getNumber() {
        return number;
    }

    public List<LootboxPrize> getPrizes() {
        return prizes;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(number, true);
        kryo.writeClassAndObject(output, prizes);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        number = input.readInt(true);
        prizes = (List<LootboxPrize>) kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        return "LootboxPrizes{" +
                "number=" + number +
                ", prizes=" + prizes +
                '}';
    }
}
