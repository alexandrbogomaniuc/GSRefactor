package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.quests.IQuest;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class PlayerQuests implements IPlayerQuests, KryoSerializable, Serializable {
    private static final byte VERSION = 0;
    private int version = 0;
    private Set<IQuest> quests;

    public PlayerQuests() {}

    public PlayerQuests(Set<IQuest> quests) {
        this.quests = quests;
    }

    @Override
    public Set<IQuest> getQuests() {
        return quests;
    }

    @Override
    public void setQuests(Set<IQuest> quests) {
        this.quests = quests;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(version, true);
        kryo.writeObject(output, quests);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        version = input.readInt(true);
        quests = kryo.readObject(input, HashSet.class);
    }

    @Override
    public String toString() {
        return "PlayerQuests[" +
                "version=" + version +
                ", quests=" + quests +
                ']';
    }
}
