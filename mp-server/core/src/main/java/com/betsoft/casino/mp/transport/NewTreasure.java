package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.quests.INewTreasure;
import com.betsoft.casino.utils.TObject;

import java.util.Objects;

public class NewTreasure extends TObject implements INewTreasure {
    private long id;
    private long enemyId;
    private long completedQuestId;
    private long questId;

    public NewTreasure(long date, int rid, long id, long enemyId, int completedQuestId, long questId) {
        super(date, rid);
        this.id = id;
        this.enemyId = enemyId;
        this.completedQuestId = completedQuestId;
        this.questId = questId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public long getEnemyId() {
        return enemyId;
    }

    @Override
    public void setEnemyId(int enemyId) {
        this.enemyId = enemyId;
    }

    @Override
    public long getCompletedQuestId() {
        return completedQuestId;
    }

    @Override
    public void setCompletedQuestId(long completedQuestId) {
        this.completedQuestId = completedQuestId;
    }

    @Override
    public long getQuestId() {
        return questId;
    }

    @Override
    public void setQuestId(long questId) {
        this.questId = questId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NewTreasure that = (NewTreasure) o;
        return id == that.id &&
                enemyId == that.enemyId &&
                completedQuestId == that.completedQuestId &&
                questId == that.questId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, enemyId, completedQuestId, questId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NewTreasure{");
        sb.append("id=").append(id);
        sb.append(", enemyId=").append(enemyId);
        sb.append(", completedQuestId=").append(completedQuestId);
        sb.append(", questId=").append(questId);
        sb.append('}');
        return sb.toString();
    }
}
