package com.betsoft.casino.mp.bgmissionamazon.model.math;

import java.util.List;

public class SpawnStageFromConfig {
    private final long time;
    private final List<Double> weightEnemies;

    public SpawnStageFromConfig(long time, List<Double> weightEnemies) {
        this.time = time;
        this.weightEnemies = weightEnemies;
    }

    public long getTime() {
        return time;
    }

    public double getEnemyWeightByEnemyRange(EnemyRange enemyRange, double defVal) {
        if (EnemyRange.HIGH_PAY_ENEMIES.equals(enemyRange)) {
            return getHPEnemyWeight();
        } else if (EnemyRange.MID_PAY_ENEMIES.equals(enemyRange)) {
            return getMPEnemyWeight();
        } else if (EnemyRange.LOW_PAY_ENEMIES.equals(enemyRange)) {
            return getLPEnemyWeight();
        } else {
            return defVal;
        }
    }

    private double getHPEnemyWeight() {
        return weightEnemies.get(0);
    }

    private double getMPEnemyWeight() {
        return weightEnemies.get(1);
    }

    private double getLPEnemyWeight() {
        return weightEnemies.get(2);
    }
}
