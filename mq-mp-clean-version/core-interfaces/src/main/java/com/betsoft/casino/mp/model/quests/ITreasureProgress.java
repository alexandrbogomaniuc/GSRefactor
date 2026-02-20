package com.betsoft.casino.mp.model.quests;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface ITreasureProgress {
    int getTreasureId();

    void setTreasureId(int treasureId);

    int getCollect();

    void setCollect(int collect);

    void addCollect(int collect);

    int getGoal();

    void setGoal(int goal);
}
