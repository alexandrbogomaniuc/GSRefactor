package com.betsoft.casino.mp.bgmissionamazon.model.math;

import com.betsoft.casino.mp.model.IEnemyRange;
import com.dgphoenix.casino.common.util.RNG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.bgmissionamazon.model.math.EnemyType.*;

public enum EnemyRange implements IEnemyRange<EnemyType> {
    BASE_ENEMIES(Arrays.asList(
            SKULL_BREAKER,
            WITCH,
            GUARDIAN,
            RUNNER,
            JAGUAR,
            SERPENT,
            ANT,
            WASP,
            ARMED_WARRIOR,
            EXPLODING_TOAD,
            SCORPION,
            TINY_TOAD,
            FLOWERS_1,
            FLOWERS_2,
            PLANT_1,
            PLANT_2,
            WEAPON_CARRIER_1,
            WEAPON_CARRIER_2,
            WEAPON_CARRIER_3,
            WEAPON_CARRIER_4,
            WEAPON_CARRIER_5
    )),

    FLYING_ENEMIES(Collections.singletonList(WASP)),

    WALKING_ENEMIES(Arrays.asList(
            SKULL_BREAKER,
            GUARDIAN,
            JAGUAR,
            EXPLODING_TOAD,
            ARMED_WARRIOR,
            SCORPION
    )),

    STATIC_ENEMIES(Arrays.asList(
            FLOWERS_1,
            FLOWERS_2,
            PLANT_1,
            PLANT_2,
            WEAPON_CARRIER_1,
            WEAPON_CARRIER_2,
            WEAPON_CARRIER_3,
            WEAPON_CARRIER_4,
            WEAPON_CARRIER_5,
            WITCH
    )),

    WEAPON_CARRIERS(Arrays.asList(
            WEAPON_CARRIER_1,
            WEAPON_CARRIER_2,
            WEAPON_CARRIER_3,
            WEAPON_CARRIER_4,
            WEAPON_CARRIER_5
    )),

    FLOWERS(Arrays.asList(
            FLOWERS_1,
            FLOWERS_2
    )),

    PLANTS(Arrays.asList(
            PLANT_1,
            PLANT_2
    )),

    LOW_PAY_ENEMIES(Arrays.asList(
            RUNNER,
            TINY_TOAD
    )),

    MID_PAY_ENEMIES(Arrays.asList(
            ARMED_WARRIOR,
            SCORPION,
            EXPLODING_TOAD
    )),

    HIGH_PAY_ENEMIES(Arrays.asList(
            SKULL_BREAKER,
            GUARDIAN,
            JAGUAR
    )),

    SWARM_ENEMIES(Arrays.asList(
            SERPENT,
            ANT,
            WASP
    ));

    public static final List<Integer> WEAPON_CARRIERS_IDS = getIdsFromRange(WEAPON_CARRIERS);
    public static final List<Integer> STATIC_ENEMIES_IDS = getIdsFromRange(STATIC_ENEMIES);

    private static List<Integer> getIdsFromRange(EnemyRange enemyRange) {
        return getEnemiesFromRanges(enemyRange).stream()
                .map(EnemyType::getId)
                .collect(Collectors.toList());
    }


    private List<EnemyType> enemies;

    @Override
    public List<EnemyType> getEnemies() {
        return enemies;
    }

    public EnemyType getRandomEnemy() {
        return enemies.get(RNG.nextInt(enemies.size()));
    }

    EnemyRange(List<EnemyType> enemies) {
        this.enemies = enemies;
    }

    public boolean contains(EnemyType enemyType) {
        return getEnemies().contains(enemyType);
    }

    public static List<EnemyType> getEnemiesFromRanges(EnemyRange... enemyRanges) {
        List<EnemyType> enemies = new ArrayList<>();
        Arrays.stream(enemyRanges).forEach(enemyRange -> enemies.addAll(enemyRange.getEnemies()));
        return enemies;
    }

}
