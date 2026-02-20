package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.utils.TObject;

import java.util.Set;

public class Quests extends TObject {
    private Set<IQuest> quests;

    public Quests(long date, int rid, Set<IQuest> quests) {
        super(date, rid);
        this.quests = quests;
    }

    @Override
    public String toString() {
        return "Quests[" +
                ", quests=" + quests +
                "] " + super.toString();
    }
}

