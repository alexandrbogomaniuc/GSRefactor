package com.betsoft.casino.mp.bgsectorx.model.math;

import com.betsoft.casino.mp.model.IEnemyRange;
import com.dgphoenix.casino.common.util.RNG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.bgsectorx.model.math.EnemyType.*;

public enum EnemyRange implements IEnemyRange<EnemyType> {
    BASE_ENEMIES(Arrays.asList(
            S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, S16, S17, S18, S19, S20,
            S21, S22, S23, S24, S25, S26, S27, S28, S29, S30, S31, B3, B2, B1
    )),

    SPECIAL_ITEMS(Arrays.asList(F1, F2, F3, F4, F5, F6, F7)),

    LOW_PAY_ENEMIES(Arrays.asList(
            S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13
    )),

    MID_PAY_ENEMIES(Arrays.asList(
            S14, S15, S16, S17, S18, S19, S20, S21, S22, S23, S24, S25, S26
    )),

    HIGH_PAY_ENEMIES(Arrays.asList(
            S27, S28, S29, S30, S31
    )),

    HUGE_PAY_ENEMIES(Arrays.asList(
            B3, B2, B1
    ));

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
