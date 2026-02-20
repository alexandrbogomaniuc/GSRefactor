package com.betsoft.casino.mp.sectorx.model.math;

import java.util.List;
import java.util.StringJoiner;

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


    public double getEnemyWeightByEnemyType(EnemyType enemyType, double defVal) {
        if (EnemyRange.LOW_PAY_ENEMIES.getEnemies().contains(enemyType)) {
            return getHPEnemyWeight();
        } else if(EnemyRange.MID_PAY_ENEMIES.getEnemies().contains(enemyType)) {
            return getMPEnemyWeight();
        } else if (EnemyRange.HIGH_PAY_ENEMIES.getEnemies().contains(enemyType)) {
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

    @Override
    public String toString() {
        return new StringJoiner(", ", SpawnStageFromConfig.class.getSimpleName() + "[", "]")
                .add("time=" + time)
                .add("weightEnemies=" + weightEnemies)
                .toString();
    }
}
