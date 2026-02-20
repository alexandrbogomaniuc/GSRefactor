package com.betsoft.casino.mp.piratespov.model;

import com.betsoft.casino.mp.model.IEnemyRange;
import com.betsoft.casino.mp.piratescommon.model.math.EnemyType;

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
            EnemyType.ENEMY_14,
            EnemyType.ENEMY_15,
            EnemyType.ENEMY_16,
            EnemyType.ENEMY_17,
            EnemyType.ENEMY_18,
            EnemyType.ENEMY_19,
            EnemyType.ENEMY_20,
            EnemyType.WEAPON_CARRIER
    )),

    Boss(Collections.singletonList(EnemyType.Boss)),
    MINI_BOSS(Collections.singletonList(EnemyType.ENEMY_20)),
    RUNNER(Collections.singletonList(EnemyType.ENEMY_17)),
    BIRDS(Arrays.asList(EnemyType.ENEMY_9, EnemyType.ENEMY_10)),
    WEAPON_CARRIER(Collections.singletonList(EnemyType.WEAPON_CARRIER)),
    LOOT_RUNNER(Collections.singletonList(EnemyType.ENEMY_18)),
    Mummies(Arrays.asList(
            EnemyType.ENEMY_11,
            EnemyType.ENEMY_12,
            EnemyType.ENEMY_13,
            EnemyType.ENEMY_14,
            EnemyType.ENEMY_15,
            EnemyType.ENEMY_16
    )),

    MUMMIES_WITHOUT_DECKHANDS(Arrays.asList(
            EnemyType.ENEMY_15,
            EnemyType.ENEMY_16
    )),
    Scarabs(Arrays.asList(
            EnemyType.ENEMY_1,
            EnemyType.ENEMY_2,
            EnemyType.ENEMY_3,
            EnemyType.ENEMY_4,
            EnemyType.ENEMY_5,
            EnemyType.ENEMY_6,
            EnemyType.ENEMY_7,
            EnemyType.ENEMY_8
    )),
    RATS(Arrays.asList(EnemyType.ENEMY_1, EnemyType.ENEMY_2, EnemyType.ENEMY_3)),
    LARGE_CRABS(Arrays.asList(
            EnemyType.ENEMY_4,
            EnemyType.ENEMY_6,
            EnemyType.ENEMY_7,
            EnemyType.ENEMY_8
    )),
    SMALL_CRABS(Collections.singletonList(EnemyType.ENEMY_5)),

    HV_ENEMIES(Arrays.asList(
            EnemyType.ENEMY_17,
            EnemyType.ENEMY_18,
            EnemyType.ENEMY_19,
            EnemyType.WEAPON_CARRIER,
            EnemyType.ENEMY_20
    )),

    TROLLS(Arrays.asList(
            EnemyType.ENEMY_19,
            EnemyType.ENEMY_20
    )),

    RUNNERS(Arrays.asList(
            EnemyType.ENEMY_17,
            EnemyType.ENEMY_18
    )),

    MID_VALUE_ENEMIES_WITHOUT_RUNNERS(Arrays.asList(
            EnemyType.ENEMY_13,
            EnemyType.ENEMY_14
    )),

    CAPTAINS(Arrays.asList(
            EnemyType.ENEMY_15,
            EnemyType.ENEMY_16
    )),

    ENEMIES_DECKHAND(Arrays.asList(
            EnemyType.ENEMY_11,
            EnemyType.ENEMY_12
    )),

    ENEMIES_NECKBEARD(Arrays.asList(
            EnemyType.ENEMY_13,
            EnemyType.ENEMY_14
    )),

    SINGLE_ENEMIES(Arrays.asList(
            EnemyType.ENEMY_11,
            EnemyType.ENEMY_12,
            EnemyType.ENEMY_13,
            EnemyType.ENEMY_14
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
