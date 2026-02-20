package com.betsoft.casino.mp.model.quests;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.Objects;

public class Quest implements IQuest, KryoSerializable, Serializable {
    private static final byte VERSION = 0;
    private long id;
    private int type;
    private long roomCoin;
    private boolean needReset;
    private long collectedAmount;
    private String name;
    private QuestProgress progress;
    private QuestPrize questPrize;

    public Quest() {}

    public Quest(long id, int type, long roomCoin, boolean needReset, long collectedAmount, QuestProgress progress,
                 QuestPrize questPrize, String name) {
        this.id = id;
        this.type = type;
        this.roomCoin = roomCoin;
        this.needReset = needReset;
        this.collectedAmount = collectedAmount;
        this.progress = progress;
        this.questPrize = questPrize;
        this.name = name;
    }

    public static Quest convert(IQuest quest) {
        return new Quest(quest.getId(), quest.getType(), quest.getRoomCoin(), quest.isNeedReset(),
                quest.getCollectedAmount(), QuestProgress.convert(quest.getProgress()),
                quest.getQuestPrize() == null ? null : QuestPrize.convert(quest.getQuestPrize()),
                quest.getName());
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public long getRoomCoin() {
        return roomCoin;
    }

    public void setRoomCoin(long roomCoin) {
        this.roomCoin = roomCoin;
    }

    @Override
    public boolean isNeedReset() {
        return needReset;
    }

    @Override
    public void setNeedReset(boolean needReset) {
        this.needReset = needReset;
    }

    @Override
    public long getCollectedAmount() {
        return collectedAmount;
    }

    @Override
    public void setCollectedAmount(long collectedAmount) {
        this.collectedAmount = collectedAmount;
    }

    @Override
    public IQuestProgress getProgress() {
        return progress;
    }

    public void setProgress(QuestProgress progress) {
        this.progress = progress;
    }

    @Override
    public QuestPrize getQuestPrize() {
        return questPrize;
    }

    public void setQuestPrize(QuestPrize questPrize) {
        this.questPrize = questPrize;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Quest[");
        sb.append("id=").append(id);
        sb.append(", type=").append(type);
        sb.append(", roomCoin=").append(roomCoin);
        sb.append(", needReset=").append(needReset);
        sb.append(", name=").append(name);
        sb.append(", collectedAmount=").append(collectedAmount);
        sb.append(progress);
        sb.append(questPrize);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeInt(type, true);
        output.writeLong(roomCoin, true);
        output.writeBoolean(needReset);
        output.writeString(name);
        output.writeLong(collectedAmount, true);
        kryo.writeClassAndObject(output, progress);
        kryo.writeClassAndObject(output, questPrize);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        id = input.readLong(true);
        type = input.readInt(true);
        roomCoin = input.readLong(true);
        needReset = input.readBoolean();
        name = input.readString();
        collectedAmount = input.readLong(true);
        progress = (QuestProgress) kryo.readClassAndObject(input);
        questPrize = (QuestPrize) kryo.readClassAndObject(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quest quest = (Quest) o;
        return id == quest.id &&
                type == quest.type &&
                roomCoin == quest.roomCoin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, roomCoin);
    }


}
