package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.IPlayerQuests;
import com.betsoft.casino.mp.model.quests.IQuest;

import java.util.Set;

public class StubPlayerQuests implements IPlayerQuests {
    private int version = 0;
    private Set<IQuest> quests;

    public StubPlayerQuests(Set<IQuest> quests) {
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
    public String toString() {
        return "PlayerQuests[" +
                "version=" + version +
                ", quests=" + quests +
                ']';
    }
}
