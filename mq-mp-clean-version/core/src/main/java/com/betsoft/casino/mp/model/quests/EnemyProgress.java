package com.betsoft.casino.mp.model.quests;

import com.betsoft.casino.mp.model.IEnemyProgress;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.Objects;

public class EnemyProgress implements IEnemyProgress, KryoSerializable, Serializable {
    private static final byte VERSION = 0;
    private int typeId;
    private int skin;
    private int kills;
    private int goal;

    public EnemyProgress() {}

    public EnemyProgress(int typeId, int skin, int kills, int goal) {
        this.typeId = typeId;
        this.skin = skin;
        this.kills = kills;
        this.goal = goal;
    }

    @Override
    public int getTypeId() {
        return typeId;
    }

    @Override
    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    @Override
    public int getSkin() {
        return skin;
    }

    @Override
    public void setSkin(int skin) {
        this.skin = skin;
    }

    @Override
    public int getKills() {
        return kills;
    }

    @Override
    public void setKills(int kills) {
        this.kills = kills;
    }

    @Override
    public void addKills(int kills) {
        this.kills += kills;
    }

    @Override
    public void incrementKills() {
        this.kills++;
    }

    @Override
    public int getGoal() {
        return goal;
    }

    @Override
    public void setGoal(int goal) {
        this.goal = goal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnemyProgress that = (EnemyProgress) o;
        return typeId == that.typeId &&
                skin == that.skin &&
                kills == that.kills &&
                goal == that.goal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeId, skin, kills, goal);
    }

    @Override
    public String toString() {
        return "EnemyProgress[" +
                "typeId=" + typeId +
                ", skin=" + skin +
                ", kills=" + kills +
                ", goal=" + goal +
                ']';
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(typeId, true);
        output.writeInt(skin, true);
        output.writeInt(kills, true);
        output.writeInt(goal, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        typeId = input.readInt(true);
        skin = input.readInt(true);
        kills = input.readInt(true);
        goal = input.readInt(true);
    }
}
