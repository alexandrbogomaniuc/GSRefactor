package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 15.05.2020.
 */
public interface IMathEnemy {
    int getIndex();

    void setIndex(int index);

    String getTypeName();

    void setTypeName(String typeName);

    int getSettingsEnemyId();

    void setSettingsEnemyId(int settingsEnemyId);

    double getFullEnergy();

    void setFullEnergy(double fullEnergy);
}
