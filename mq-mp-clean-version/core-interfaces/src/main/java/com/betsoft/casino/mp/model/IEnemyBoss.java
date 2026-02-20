package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.gameconfig.BossPartEnemy;

import java.util.List;

/**
 * User: flsh
 * Date: 27.05.2020.
 */
public interface IEnemyBoss {
    List<BossPartEnemy> getHeadEnemies();

    List<BossPartEnemy> getTailEnemies();
}
