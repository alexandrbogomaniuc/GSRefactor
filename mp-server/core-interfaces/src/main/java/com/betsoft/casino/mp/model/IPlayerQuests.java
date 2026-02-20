package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.quests.IQuest;

import java.util.Set;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IPlayerQuests {
    Set<IQuest> getQuests();

    void setQuests(Set<IQuest> quests);
}
