package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.TObject;

import java.util.Objects;

public class RemoveQuest extends TObject implements IServerMessage {
    private long questId;

    public RemoveQuest(long date, long questId) {
        super(date, SERVER_RID);
        this.questId = questId;
    }

    public long getQuestId() {
        return questId;
    }

    public void setQuestId(long questId) {
        this.questId = questId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RemoveQuest that = (RemoveQuest) o;
        return questId == that.questId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), questId);
    }

    @Override
    public String toString() {
        return "RemoveQuest[" +
                "date=" + date +
                ", rid=" + rid +
                "] " + super.toString();
    }
}
