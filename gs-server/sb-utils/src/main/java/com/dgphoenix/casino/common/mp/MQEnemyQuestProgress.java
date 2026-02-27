package com.abs.casino.common.mp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class MQEnemyQuestProgress implements KryoSerializable {
    private static final byte VERSION = 0;

    private int typeId;
    private int skin;
    private int kills;
    private int goal;

    public MQEnemyQuestProgress() { }

    public MQEnemyQuestProgress(int typeId, int skin, int kills, int goal) {
        this.typeId = typeId;
        this.skin = skin;
        this.kills = kills;
        this.goal = goal;
    }

    public int getTypeId() {
        return typeId;
    }

    public int getSkin() {
        return skin;
    }

    public int getKills() {
        return kills;
    }

    public int getGoal() {
        return goal;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(typeId, true);
        output.writeInt(skin, true);
        output.writeInt(kills, true);
        output.writeInt(goal, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        typeId = input.readInt(true);
        skin = input.readInt(true);
        kills = input.readInt(true);
        goal = input.readInt(true);
    }

    @Override
    public String toString() {
        return "MQEnemyQuestProgress{" +
                "typeId=" + typeId +
                ", skin=" + skin +
                ", kills=" + kills +
                ", goal=" + goal +
                '}';
    }
}
