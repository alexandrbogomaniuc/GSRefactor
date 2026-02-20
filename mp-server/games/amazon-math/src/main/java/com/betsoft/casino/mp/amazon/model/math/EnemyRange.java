package com.betsoft.casino.mp.amazon.model.math;

import com.betsoft.casino.mp.model.IEnemyRange;
import com.betsoft.casino.mp.amazon.model.math.EnemyType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum EnemyRange implements IEnemyRange<EnemyType> {
    BaseEnemies(Arrays.asList(
            EnemyType.SKULL_BREAKER,
            EnemyType.SHAMAN,
            EnemyType.JUMPER,
            EnemyType.RUNNER,
            EnemyType.JAGUAR,
            EnemyType.SNAKE,
            EnemyType.ANT,
            EnemyType.WASP,
            EnemyType.WEAPON_CARRIER,
            EnemyType.EXPLODER,
            EnemyType.MULTIPLIER
    )),

    ExplodeEnemies(Arrays.asList(
            EnemyType.SKULL_BREAKER,
            EnemyType.SHAMAN,
            EnemyType.JUMPER,
            EnemyType.RUNNER,
            EnemyType.JAGUAR,
            EnemyType.SNAKE,
            EnemyType.ANT,
            EnemyType.WASP
    )),

    Boss(Collections.singletonList(EnemyType.Boss)),
    MINI_BOSS(Collections.singletonList(EnemyType.MULTIPLIER)),
    RUNNER(Collections.singletonList(EnemyType.RUNNER)),
    BIRDS(Arrays.asList(EnemyType.WASP)),
    WEAPON_CARRIER(Collections.singletonList(EnemyType.WEAPON_CARRIER)),
    LOOT_RUNNER(Collections.singletonList(EnemyType.RUNNER)),
    Mummies(Arrays.asList(
            EnemyType.SKULL_BREAKER,
            EnemyType.SHAMAN,
            EnemyType.JUMPER,
            EnemyType.JAGUAR,
            EnemyType.EXPLODER,
            EnemyType.WEAPON_CARRIER,
            EnemyType.MULTIPLIER
            )),
    Scarabs(Arrays.asList(
            EnemyType.WASP,
            EnemyType.ANT,
            EnemyType.SNAKE
    )),

    LargeEnemies(Arrays.asList(
            EnemyType.MULTIPLIER,
            EnemyType.JAGUAR,
            EnemyType.WEAPON_CARRIER,
            EnemyType.EXPLODER
    )),

    HV_ENEMIES(
            Arrays.asList(
                    EnemyType.EXPLODER,
                    EnemyType.WEAPON_CARRIER,
                    EnemyType.MULTIPLIER
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
