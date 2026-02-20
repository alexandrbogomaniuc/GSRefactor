package com.betsoft.casino.mp.model;

public interface IEnemyLimitChecker<ENEMY extends IEnemy> {

    void reset();
    void countEnemy(ENEMY enemy);
}
