package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.quests.Quest;
import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.TObject;

import java.util.Objects;

public class NewQuest extends TObject implements IServerMessage {
    private Quest quest;
    private long sourceEnemyId;

    public NewQuest(long date, Quest quest, long sourceEnemyId) {
        super(date, SERVER_RID);
        this.quest = quest;
        this.sourceEnemyId = sourceEnemyId;
    }

    public Quest getQuest() {
        return quest;
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    public long getSourceEnemyId() {
        return sourceEnemyId;
    }

    public void setSourceEnemyId(long sourceEnemyId) {
        this.sourceEnemyId = sourceEnemyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NewQuest newQuest = (NewQuest) o;
        return sourceEnemyId == newQuest.sourceEnemyId &&
                Objects.equals(quest, newQuest.quest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), quest, sourceEnemyId);
    }

    @Override
    public String toString() {
        return "NewQuest[" +
                "sourceEnemyId=" + sourceEnemyId +
                "quest=" + quest +
                "] " + super.toString();
    }
}
