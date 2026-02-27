package com.abs.casino.common.mp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class MQTreasureQuestProgress implements KryoSerializable {
    private static final byte VERSION = 0;

    private int treasureId;
    private int collect;
    private int goal;

    public MQTreasureQuestProgress() {}

    public MQTreasureQuestProgress(int treasureId, int collect, int goal) {
        this.treasureId = treasureId;
        this.collect = collect;
        this.goal = goal;
    }

    public int getTreasureId() {
        return treasureId;
    }

    public int getCollect() {
        return collect;
    }

    public int getGoal() {
        return goal;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(treasureId, true);
        output.writeInt(collect, true);
        output.writeInt(goal, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        treasureId = input.readInt(true);
        collect = input.readInt(true);
        goal = input.readInt(true);
    }

    @Override
    public String toString() {
        return "MQTreasureQuestProgress{" +
                "treasureId=" + treasureId +
                ", collect=" + collect +
                ", goal=" + goal +
                '}';
    }
}
