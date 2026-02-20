package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.model.quests.IQuestPrize;
import com.betsoft.casino.mp.model.quests.IQuestProgress;

import java.util.Objects;

public class StubQuest implements IQuest {
    private long id;
    private int type;
    private long roomCoin;
    private boolean needReset;
    private long collectedAmount;
    private String name;
    private IQuestProgress progress;
    private IQuestPrize questPrize;

    public StubQuest(long id, int type, long roomCoin, boolean needReset, long collectedAmount, IQuestProgress progress,
                     IQuestPrize questPrize, String name) {
        this.id = id;
        this.type = type;
        this.roomCoin = roomCoin;
        this.needReset = needReset;
        this.collectedAmount = collectedAmount;
        this.progress = progress;
        this.questPrize = questPrize;
        this.name = name;
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

    public void setProgress(IQuestProgress progress) {
        this.progress = progress;
    }

    @Override
    public IQuestPrize getQuestPrize() {
        return questPrize;
    }

    public void setQuestPrize(IQuestPrize questPrize) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubQuest quest = (StubQuest) o;
        return id == quest.id &&
                type == quest.type &&
                roomCoin == quest.roomCoin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, roomCoin);
    }


}
