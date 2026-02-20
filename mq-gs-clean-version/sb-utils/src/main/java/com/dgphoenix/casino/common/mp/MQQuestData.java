package com.dgphoenix.casino.common.mp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.List;

public class MQQuestData implements KryoSerializable {
    private static final byte VERSION = 0;

    private long id;
    private int type;
    private long roomCoin;
    private boolean needReset;
    private long collectedAmount;
    private String name;
    private List<MQTreasureQuestProgress> treasures = new ArrayList<MQTreasureQuestProgress>();
    private MQuestPrize questPrize;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getRoomCoin() {
        return roomCoin;
    }

    public void setRoomCoin(long roomCoin) {
        this.roomCoin = roomCoin;
    }

    public boolean isNeedReset() {
        return needReset;
    }

    public void setNeedReset(boolean needReset) {
        this.needReset = needReset;
    }

    public long getCollectedAmount() {
        return collectedAmount;
    }

    public void setCollectedAmount(long collectedAmount) {
        this.collectedAmount = collectedAmount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MQuestPrize getQuestPrize() {
        return questPrize;
    }

    public void setQuestPrize(MQuestPrize questPrize) {
        this.questPrize = questPrize;
    }

    public List<MQTreasureQuestProgress> getTreasures() {
        return treasures;
    }

    public void setTreasures(List<MQTreasureQuestProgress> treasures) {
        this.treasures = treasures;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeInt(type, true);
        output.writeLong(roomCoin, true);
        output.writeBoolean(needReset);
        output.writeLong(collectedAmount, true);
        output.writeString(name);
        kryo.writeObject(output, treasures);
        kryo.writeObject(output, questPrize);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        id = input.readLong(true);
        type = input.readInt(true);
        roomCoin = input.readLong(true);
        needReset = input.readBoolean();
        collectedAmount = input.readLong(true);
        name = input.readString();
        treasures = kryo.readObject(input, ArrayList.class);
        questPrize = kryo.readObject(input, MQuestPrize.class);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MQQuestData{");
        sb.append("id=").append(id);
        sb.append(", type=").append(type);
        sb.append(", roomCoin=").append(roomCoin);
        sb.append(", needReset=").append(needReset);
        sb.append(", collectedAmount=").append(collectedAmount);
        sb.append(", name='").append(name).append('\'');
        sb.append(", treasures=").append(treasures);
        sb.append(", questPrize=").append(questPrize);
        sb.append('}');
        return sb.toString();
    }
}
