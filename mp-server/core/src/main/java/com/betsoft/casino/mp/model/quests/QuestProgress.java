package com.betsoft.casino.mp.model.quests;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuestProgress implements IQuestProgress<TreasureProgress>, KryoSerializable, Serializable {
    private static final byte VERSION = 0;
    private List<TreasureProgress> treasures;

    public QuestProgress() {}

    public QuestProgress(List<ITreasureProgress> treasures) {
        this.treasures = treasures == null ? null : TreasureProgress.convert(treasures);
    }

    public static QuestProgress convert(IQuestProgress progress) {
        return new QuestProgress(progress == null ? null : progress.getTreasures());
    }

    @Override
    public List<TreasureProgress> getTreasures() {
        return treasures;
    }

    @Override
    public void setTreasures(List<TreasureProgress> treasures) {
        this.treasures = treasures;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestProgress that = (QuestProgress) o;
        return Objects.equals(treasures, that.treasures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(treasures);
    }

    @Override
    public String toString() {
        return "[" + treasures + ']';
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeClassAndObject(output, treasures);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        treasures = (List<TreasureProgress>) kryo.readClassAndObject(input);
    }

    @Override
    public void resetProgress() {
        for (ITreasureProgress treasure : treasures) {
            treasure.setCollect(0);
        }
    }

    @Override
    public void decreaseProgress() {
        for (ITreasureProgress treasure : treasures) {
            treasure.setCollect(treasure.getCollect() - 1);
        }
    }

}