package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 06.05.2022.
 */
public interface IActionGamePlayerRoundInfo<ENEMY_TYPE extends IEnemyType, ENEMY_STAT extends IEnemyStat, PRI extends IPlayerRoundInfo>
        extends IPlayerRoundInfo<ENEMY_TYPE, ENEMY_STAT, PRI> {
    int getShotsCount();

    void addShotsCount(long shotsCount);

    void setShotsCount(int shotsCount);

    Money getTotalBetsSpecialWeapons();

    void setTotalBetsSpecialWeapons(Money totalBetsSpecialWeapons);

    void addTotalBetsSpecialWeapons(Money totalBetsSpecialWeapons);


}
