package com.betsoft.casino.mp.model.quests;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

public class QuestAmount implements IQuestAmount, KryoSerializable, Serializable {
    private static final byte VERSION = 0;
    private int from;
    private int to;

    public QuestAmount() {}

    public QuestAmount(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public static QuestAmount convert(IQuestAmount amount) {
        return new QuestAmount(amount.getFrom(), amount.getTo());
    }

    @Override
    public int getFrom() {
        return from;
    }

    @Override
    public void setFrom(int from) {
        this.from = from;
    }

    @Override
    public int getTo() {
        return to;
    }

    @Override
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
        final StringBuilder sb = new StringBuilder("QuestAmount[");
        sb.append("from=").append(from);
        sb.append(", to=").append(to);
        sb.append(']');
        return sb.toString();
    }
}
