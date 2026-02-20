package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 25.05.2020.
 */
public interface IEnemyProgress {
    int getTypeId();

    void setTypeId(int typeId);

    int getSkin();

    void setSkin(int skin);

    int getKills();

    void setKills(int kills);

    void addKills(int kills);

    void incrementKills();

    int getGoal();

    void setGoal(int goal);
}
