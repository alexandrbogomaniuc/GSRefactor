package com.betsoft.casino.mp.clashofthegods.model.math;

import com.betsoft.casino.mp.clashofthegods.model.math.enemies.Phoenix;
import com.betsoft.casino.mp.model.IEnemyRange;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum EnemyRange implements IEnemyRange<EnemyType> {
    BaseEnemies(Arrays.asList(
            EnemyType.Evil_Spirit,
            EnemyType.Evil_Spirit_1,
            EnemyType.Evil_Spirit_2,
            EnemyType.Owl,
            EnemyType.Snake,
            EnemyType.Lizard,
            EnemyType.Tiger,
            EnemyType.Silver_Dragon,
            EnemyType.Golden_Dragon,
            EnemyType.Spirits_1_RED,
            EnemyType.Spirits_2_ORANGE,
            EnemyType.Spirits_3_GREEN,
            EnemyType.Spirits_4_BLUE,
            EnemyType.Spirits_5_VIOLETT,
            EnemyType.Dragonfly_Green,
            EnemyType.Dragonfly_Red,
            EnemyType.Beetle_1,
            EnemyType.Beetle_2
    )),

    FLY_ENEMIES(Arrays.asList(
            EnemyType.Owl,
            EnemyType.Golden_Dragon,
            EnemyType.Silver_Dragon,
            EnemyType.Phoenix,
            EnemyType.Lantern,
            EnemyType.Dragonfly_Green,
            EnemyType.Dragonfly_Red
            )),

    BIRDS(Arrays.asList(EnemyType.Owl)),

    DRAGONS(Arrays.asList(EnemyType.Golden_Dragon, EnemyType.Silver_Dragon)),

    Evils(Arrays.asList(
            EnemyType.Evil_Spirit, EnemyType.Evil_Spirit_1, EnemyType.Evil_Spirit_2
    )),

    Lizards(Arrays.asList(
            EnemyType.Lizard, EnemyType.Lizard_1, EnemyType.Lizard_2
    )),

    BaseEnemiesWithoutSpirits(Arrays.asList(
            EnemyType.Evil_Spirit,
            EnemyType.Evil_Spirit_1,
            EnemyType.Evil_Spirit_2,
            EnemyType.Owl,
            EnemyType.Snake,
            EnemyType.Lizard,
            EnemyType.Lizard_1,
            EnemyType.Lizard_2,
            EnemyType.Tiger,
            EnemyType.Silver_Dragon,
            EnemyType.Golden_Dragon,
            EnemyType.Dragonfly_Green,
            EnemyType.Dragonfly_Red,
            EnemyType.Beetle_1,
            EnemyType.Beetle_2,
            EnemyType.Phoenix,
            EnemyType.Lantern
    )),

    Boss(Collections.singletonList(EnemyType.Boss)),

    BOMB_ENEMY(Arrays.asList(EnemyType.Spirits_1_RED,
            EnemyType.Spirits_2_ORANGE,
            EnemyType.Spirits_3_GREEN,
            EnemyType.Spirits_4_BLUE,
            EnemyType.Spirits_5_VIOLETT)),

    HORUS(Collections.singletonList(EnemyType.Snake)),

    JUMP_ENEMIES(Arrays.asList(
            EnemyType.Tiger
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
