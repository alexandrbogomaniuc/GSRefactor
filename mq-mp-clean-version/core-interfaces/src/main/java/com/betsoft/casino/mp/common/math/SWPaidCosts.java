package com.betsoft.casino.mp.common.math;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.Serializable;


public class SWPaidCosts implements KryoSerializable, Serializable {
    private static final byte VERSION = 0;
    private int id;
    private int costMultiplier;

    public SWPaidCosts() {}

    public SWPaidCosts(int id, int costMultiplier) {
        this.id = id;
        this.costMultiplier = costMultiplier;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCostMultiplier() {
        return costMultiplier;
    }

    public void setCostMultiplier(int costMultiplier) {
        this.costMultiplier = costMultiplier;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(id, true);
        output.writeInt(costMultiplier, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        id = input.readInt(true);
        costMultiplier = input.readInt(true);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SWPaidCosts{");
        sb.append("id=").append(id);
        sb.append(", costMultiplier=").append(costMultiplier);
        sb.append('}');
        return sb.toString();
    }
}
