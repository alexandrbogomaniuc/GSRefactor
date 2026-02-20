package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.model.quests.IUpdateQuest;
import com.betsoft.casino.mp.model.quests.Quest;
import com.betsoft.casino.utils.TObject;

import java.util.Objects;

public class UpdateQuest extends TObject implements IUpdateQuest<Quest> {
    private Quest quest;
    private long lastEnemyId;

    public UpdateQuest(long date, IQuest quest, long lastEnemyId) {
        super(date, SERVER_RID);
        this.quest = Quest.convert(quest);
        this.lastEnemyId = lastEnemyId;
    }

    @Override
    public Quest getQuest() {
        return quest;
    }

    @Override
    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    @Override
    public long getSourceEnemyId() {
        return lastEnemyId;
    }

    @Override
    public void setSourceEnemyId(long sourceEnemyId) {
        this.lastEnemyId = sourceEnemyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UpdateQuest that = (UpdateQuest) o;
        return lastEnemyId == that.lastEnemyId &&
                Objects.equals(quest, that.quest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), quest, lastEnemyId);
    }

    @Override
    public String toString() {
        return "UpdateQuest[" +
                "quest=" + quest +
                ", lastEnemyId=" + lastEnemyId +
                "] " + super.toString();
    }
}

