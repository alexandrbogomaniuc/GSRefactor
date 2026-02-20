package com.betsoft.casino.mp.sectorx.model.math;

import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import static com.betsoft.casino.mp.sectorx.model.math.EnemyRange.*;

public enum SpawnStage {
    FIRST_STAGE(40_000, ImmutableMap.of(LOW_PAY_ENEMIES, 0.7, MID_PAY_ENEMIES, 0.3, HIGH_PAY_ENEMIES, 0.0)),
    SECOND_STAGE(80_000, ImmutableMap.of(LOW_PAY_ENEMIES, 0.5, MID_PAY_ENEMIES, 0.5, HIGH_PAY_ENEMIES, 0.0)),
    THIRD_STAGE(160_000, ImmutableMap.of(LOW_PAY_ENEMIES, 0.45, MID_PAY_ENEMIES, 0.45, HIGH_PAY_ENEMIES, 0.10)),
    FOURTH_STAGE(220_000, ImmutableMap.of(LOW_PAY_ENEMIES, 0.40, MID_PAY_ENEMIES, 0.40, HIGH_PAY_ENEMIES, 0.20)),
    FIFTH_STAGE(300_000, ImmutableMap.of(LOW_PAY_ENEMIES, 0.333, MID_PAY_ENEMIES, 0.333, HIGH_PAY_ENEMIES, 0.3334));

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

    public static SpawnStageFromConfig getStageWeights(long startRoundTime, List<SpawnStageFromConfig> spawnStages) {
        long time = System.currentTimeMillis() - startRoundTime;
        for (SpawnStageFromConfig stage : spawnStages) {
            if (time < stage.getTime()) {
                return stage;
            }
        }
        return spawnStages.get(spawnStages.size() - 1);
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
