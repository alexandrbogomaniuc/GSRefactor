package com.betsoft.casino.mp.bgmissionamazon.model.math;

import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import static com.betsoft.casino.mp.bgmissionamazon.model.math.EnemyRange.*;

public enum SpawnStage {
    FIRST_STAGE(40_000, ImmutableMap.of(LOW_PAY_ENEMIES, 0.8, MID_PAY_ENEMIES, 0.2, HIGH_PAY_ENEMIES, 0.0)),
    SECOND_STAGE(80_000, ImmutableMap.of(LOW_PAY_ENEMIES, 0.65, MID_PAY_ENEMIES, 0.25, HIGH_PAY_ENEMIES, 0.1)),
    THIRD_STAGE(160_000, ImmutableMap.of(LOW_PAY_ENEMIES, 0.55, MID_PAY_ENEMIES, 0.20, HIGH_PAY_ENEMIES, 0.25)),
    FOURTH_STAGE(220_000, ImmutableMap.of(LOW_PAY_ENEMIES, 0.30, MID_PAY_ENEMIES, 0.25, HIGH_PAY_ENEMIES, 0.45)),
    FIFTH_STAGE(300_000, ImmutableMap.of(LOW_PAY_ENEMIES, 0.20, MID_PAY_ENEMIES, 0.35, HIGH_PAY_ENEMIES, 0.45));

    private static final double[] initialDividers = new double[]{1, 2, 3, 4, 5, 6};
    private static final Map<EnemyRange, double[]> initialWeights = ImmutableMap.of(
            LOW_PAY_ENEMIES, new double[]{1, 0, 0, 0, 0, 0},
            MID_PAY_ENEMIES, new double[]{0.25, 0.40, 0.20, 0.05, 0.05, 0.05},
            HIGH_PAY_ENEMIES, new double[]{0.10, 0.30, 0.20, 0.20, 0.10, 0.10});

    private long time;
    private Map<EnemyRange, Double> weights;

    SpawnStage(int time, Map<EnemyRange, Double> weights) {
        this.time = time;
        this.weights = weights;
    }

    public static double getInitialWeightForEnemy(EnemyType enemyType, double sumOfEuclideanDistances, Map<EnemyRange, double[]> initialWeightsFromConfig, int[] dividersFromConfig) {
        double[] prob = initialWeightsFromConfig.getOrDefault(getRange(enemyType), new double[]{1});
        return sumOfEuclideanDistances / dividersFromConfig[GameTools.getIndexFromDoubleProb(prob)];
    }

    public static double getWeightForEnemyByTime(EnemyType enemyType, long startRoundTime, List<SpawnStageFromConfig> spawnStages) {
        return getWeights(startRoundTime, spawnStages).getEnemyWeightByEnemyRange(getRange(enemyType), 0d);
    }

    private static SpawnStageFromConfig getWeights(long startRoundTime, List<SpawnStageFromConfig> spawnStages) {
        long time = System.currentTimeMillis() - startRoundTime;
        for (SpawnStageFromConfig stage : spawnStages) {
            if (time < stage.getTime()) {
                return stage;
            }
        }
        return spawnStages.get(spawnStages.size() - 1);
    }

    private static Map<EnemyRange, Double> getWeights(long startRoundTime) {
        long time = System.currentTimeMillis() - startRoundTime;
        for (SpawnStage stage : values()) {
            if (time < stage.getTime()) {
                return stage.getWeights();
            }
        }
        return FIFTH_STAGE.getWeights();
    }

    private static EnemyRange getRange(EnemyType enemyType) {
        if (LOW_PAY_ENEMIES.contains(enemyType)) {
            return LOW_PAY_ENEMIES;
        } else if (MID_PAY_ENEMIES.contains(enemyType)) {
            return MID_PAY_ENEMIES;
        } else {
            return HIGH_PAY_ENEMIES;
        }
    }

    public long getTime() {
        return time;
    }

    public Map<EnemyRange, Double> getWeights() {
        return weights;
    }
}
