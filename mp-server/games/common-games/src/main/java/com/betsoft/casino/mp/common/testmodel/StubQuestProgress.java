package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.quests.IQuestProgress;
import com.betsoft.casino.mp.model.quests.ITreasureProgress;

import java.util.List;
import java.util.Objects;

public class StubQuestProgress implements IQuestProgress<StubTreasureProgress> {
    private List<StubTreasureProgress> treasures;

    public StubQuestProgress(List<ITreasureProgress> treasures) {
        this.treasures = treasures == null ? null : StubTreasureProgress.convert(treasures);
    }

    @Override
    public List<StubTreasureProgress> getTreasures() {
        return treasures;
    }

    @Override
    public void setTreasures(List<StubTreasureProgress> treasures) {
        this.treasures = treasures;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubQuestProgress that = (StubQuestProgress) o;
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

