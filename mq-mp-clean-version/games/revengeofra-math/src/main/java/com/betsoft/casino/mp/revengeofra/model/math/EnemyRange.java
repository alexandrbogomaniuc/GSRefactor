package com.betsoft.casino.mp.revengeofra.model.math;

import com.betsoft.casino.mp.model.IEnemyRange;
import com.betsoft.casino.mp.revengeofra.model.math.EnemyType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum EnemyRange implements IEnemyRange<EnemyType> {
    BaseEnemies(Arrays.asList(
            EnemyType.ENEMY_1,
            EnemyType.ENEMY_2,
            EnemyType.ENEMY_3,
            EnemyType.ENEMY_4,
            EnemyType.ENEMY_5,
            EnemyType.ENEMY_6,
            EnemyType.ENEMY_7,
            EnemyType.ENEMY_8,
            EnemyType.ENEMY_9,
            EnemyType.ENEMY_10,
            EnemyType.ENEMY_11,
            EnemyType.ENEMY_12,
            EnemyType.ENEMY_13,
            EnemyType.ENEMY_15,
            EnemyType.ENEMY_16,
            EnemyType.ENEMY_17,
            EnemyType.ENEMY_18,
            EnemyType.WEAPON_CARRIER
    )),

    BaseEnemiesWithoutWC(Arrays.asList(
            EnemyType.ENEMY_1,
            EnemyType.ENEMY_2,
            EnemyType.ENEMY_3,
            EnemyType.ENEMY_4,
            EnemyType.ENEMY_5,
            EnemyType.ENEMY_6,
            EnemyType.ENEMY_7,
            EnemyType.ENEMY_8,
            EnemyType.ENEMY_9,
            EnemyType.ENEMY_10,
            EnemyType.ENEMY_11,
            EnemyType.ENEMY_12,
            EnemyType.ENEMY_13,
            EnemyType.ENEMY_15,
            EnemyType.ENEMY_16,
            EnemyType.ENEMY_17,
            EnemyType.ENEMY_18
    )),

    Boss(Collections.singletonList(EnemyType.Boss)),
    WEAPON_CARRIER(Collections.singletonList(EnemyType.WEAPON_CARRIER)),

    Brawler_Berserk(Collections.singletonList(EnemyType.ENEMY_18)),

    BOMB_ENEMY(Collections.singletonList(EnemyType.ENEMY_14)),

    HORUS(Collections.singletonList(EnemyType.ENEMY_16)),

    BIRDS(Arrays.asList(EnemyType.ENEMY_6, EnemyType.ENEMY_17)),

    LOW_MUMMIES(Arrays.asList(
            EnemyType.ENEMY_8,
            EnemyType.ENEMY_9,
            EnemyType.ENEMY_10
    )),

    PHARAOH_MUMMIES(Arrays.asList(
            EnemyType.ENEMY_12,
            EnemyType.ENEMY_13
    )),

    Mummies(Arrays.asList(
            EnemyType.ENEMY_8,
            EnemyType.ENEMY_9,
            EnemyType.ENEMY_10,
            EnemyType.ENEMY_11,
            EnemyType.ENEMY_12,
            EnemyType.ENEMY_13,
            EnemyType.ENEMY_15
    )),

    JUMP_ENEMIES(Arrays.asList(
            EnemyType.ENEMY_11,
            EnemyType.ENEMY_15
    )),

    SCORPION(Collections.singletonList(EnemyType.ENEMY_7)),

    Scarabs(Arrays.asList(
            EnemyType.ENEMY_1,
            EnemyType.ENEMY_2,
            EnemyType.ENEMY_3,
            EnemyType.ENEMY_4,
            EnemyType.ENEMY_5
    )),

    HV_ENEMIES(Arrays.asList(
            EnemyType.WEAPON_CARRIER
    )),

    NON_PORTAL_ENEMIES(Arrays.asList(
            EnemyType.ENEMY_14,
            EnemyType.ENEMY_16,
            EnemyType.WEAPON_CARRIER,
            EnemyType.ENEMY_18
    ));

    private List<EnemyType> enemies;

    @Override
    public List<EnemyType> getEnemies() {
        return enemies;
    }

    EnemyRange(List<EnemyType> enemies) {
        this.enemies = enemies;
    }
}
