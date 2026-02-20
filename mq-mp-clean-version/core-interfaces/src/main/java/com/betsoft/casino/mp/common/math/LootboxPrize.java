package com.betsoft.casino.mp.common.math;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

public class LootboxPrize implements KryoSerializable, Serializable {
    private int weaponId;
    private int min;
    private int max;

    public LootboxPrize() {}

    public LootboxPrize(int weaponId, int min, int max) {
        this.weaponId = weaponId;
        this.min = min;
        this.max = max;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(weaponId, true);
        output.writeInt(min, true);
        output.writeInt(max, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        weaponId = input.readInt(true);
        min = input.readInt(true);
        max = input.readInt(true);
    }

    @Override
    public String toString() {
        return "LootboxPrize{" +
                "weaponId=" + weaponId +
                ", min=" + min +
                ", max=" + max +
                '}';
    }
}
