package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.quests.IQuestAmount;

public class StubQuestAmount implements IQuestAmount {
    private int from;
    private int to;

    public StubQuestAmount(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public int getFrom() {
        return from;
    }

    @Override
    public void setFrom(int from) {
        this.from = from;
    }

    @Override
    public int getTo() {
        return to;
    }

    @Override
    public void setTo(int to) {
        this.to = to;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QuestAmount[");
        sb.append("from=").append(from);
        sb.append(", to=").append(to);
        sb.append(']');
        return sb.toString();
    }
}

