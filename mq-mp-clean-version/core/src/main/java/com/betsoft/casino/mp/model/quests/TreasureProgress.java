package com.betsoft.casino.mp.model.quests;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TreasureProgress implements ITreasureProgress, KryoSerializable, Serializable {
    private static final byte VERSION = 0;
    private int treasureId;
    private int collect;
    private int goal;

    public TreasureProgress() {}

    public TreasureProgress(int treasureId, int collect, int goal) {
        this.treasureId = treasureId;
        this.collect = collect;
        this.goal = goal;
    }

    public static List<TreasureProgress> convert(List<ITreasureProgress> treasures) {
        List<TreasureProgress> result = new ArrayList<>();
        for (ITreasureProgress treasure : treasures) {
            result.add(new TreasureProgress(treasure.getTreasureId(), treasure.getCollect(), treasure.getGoal()));
        }
        return result;
    }

    @Override
    public int getTreasureId() {
        return treasureId;
    }

    @Override
    public void setTreasureId(int treasureId) {
        this.treasureId = treasureId;
    }

    @Override
    public int getCollect() {
        return collect;
    }

    @Override
    public void setCollect(int collect) {
        this.collect = collect;
    }

    @Override
    public void addCollect(int collect) {
        this.collect += collect;
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
        TreasureProgress that = (TreasureProgress) o;
        return treasureId == that.treasureId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(treasureId);
    }

    @Override
    public String toString() {
        return "[" +
                "treasureId=" + treasureId +
                ", collect=" + collect +
                ", goal=" + goal +
                ']';
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(treasureId, true);
        output.writeInt(collect, true);
        output.writeInt(goal, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        treasureId = input.readInt(true);
        collect = input.readInt(true);
        goal = input.readInt(true);
    }
}
