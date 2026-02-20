package com.dgphoenix.casino.common.mp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class MQuestAmount implements KryoSerializable {
    private static final byte VERSION = 0;
    private int from;
    private int to;

    public MQuestAmount() {}

    public MQuestAmount(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(from, true);
        output.writeInt(to, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        from = input.readInt(true);
        to = input.readInt(true);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MQuestAmount{");
        sb.append("from=").append(from);
        sb.append(", to=").append(to);
        sb.append('}');
        return sb.toString();
    }
}
