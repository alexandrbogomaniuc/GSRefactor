package com.betsoft.casino.mp.sectorx.model.math;

import com.betsoft.casino.mp.model.IWeight;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.sectorx.model.math.config.GameConfig;
import com.betsoft.casino.mp.movement.generators.MobOrientation;
import com.betsoft.casino.mp.movement.generators.PathParam;
import com.dgphoenix.casino.common.util.Pair;

import java.util.*;

public class MathData {
    public static final int TURRET_WEAPON_ID = -1;
    private static final Set<Integer> possibleBetLevels =
            Collections.unmodifiableSet(new TreeSet<>(Arrays.asList(1, 2, 5, 10, 20)));

    private static final double defaultMathValue = 9 * Math.PI / 4;

    private static final Map<EnemyType, PathParam> enemiesPathParams;

    static {
        enemiesPathParams = new HashMap<>();
        PathParam paramType1 = new PathParam(MobOrientation.Vert, false, defaultMathValue, defaultMathValue,
                true, 0.3, false,
                -1, -1, -1, -1, -1, -1, -1, -1);

        PathParam paramType2 = new PathParam(MobOrientation.Down_45, true, defaultMathValue, defaultMathValue,
                true, 0.05, true,
                40, 15, 15, 40, 40, 40, 15, 15);

        PathParam paramType3 = new PathParam(MobOrientation.Vert, false, defaultMathValue, defaultMathValue,
                true, 0.1, false,
                -1, -1, -1, -1, -1, -1, -1, -1);

        PathParam paramType4 = new PathParam(MobOrientation.Down_45, true, defaultMathValue, defaultMathValue,
                true, 0.1, true,
                35, 35, 35, 35, 35, 35, 25, 25);

        PathParam paramType5 = new PathParam(MobOrientation.Down_45, true, defaultMathValue, defaultMathValue,
                true, 0.05, true,
                30, 35, 30, 35, 15, 15, 15, 15);

        PathParam paramType6 = new PathParam(MobOrientation.Down_135, true, defaultMathValue, defaultMathValue,
                true, 0.05, true,
                30, 35, 30, 35, 15, 15, 15, 15);

        enemiesPathParams.put(EnemyType.S1, paramType1);
        enemiesPathParams.put(EnemyType.S2, paramType2);
        enemiesPathParams.put(EnemyType.S3, paramType3);
        enemiesPathParams.put(EnemyType.S4, paramType3);
        enemiesPathParams.put(EnemyType.S5, paramType3);
        enemiesPathParams.put(EnemyType.S6, paramType3);
        enemiesPathParams.put(EnemyType.S7, new PathParam(MobOrientation.Down_45, true, defaultMathValue, defaultMathValue,
                true, 0.3, false,
                -1, -1, -1, -1, -1, -1, -1, -1));
        enemiesPathParams.put(EnemyType.S8, paramType2);
        enemiesPathParams.put(EnemyType.S9, paramType4);
        enemiesPathParams.put(EnemyType.S10, paramType4);
        enemiesPathParams.put(EnemyType.S11, paramType4);
        enemiesPathParams.put(EnemyType.S12, paramType4);

        enemiesPathParams.put(EnemyType.S13, new PathParam(MobOrientation.Vert, false, defaultMathValue, defaultMathValue,
                true, 0.1, true,
                25, 25, 25, 25, 25, 25, 25, 25));
        enemiesPathParams.put(EnemyType.S14, new PathParam(MobOrientation.Down_45, true, defaultMathValue, defaultMathValue,
                true, 0.1, true,
                40, 15, 15, 40, 40, 40, 15, 15));
        enemiesPathParams.put(EnemyType.S15, new PathParam(MobOrientation.Vert, false, defaultMathValue, defaultMathValue,
                true, 0.2, false,
                -1, -1, -1, -1, -1, -1, -1, -1));

        enemiesPathParams.put(EnemyType.S16, new PathParam(MobOrientation.Down_45, true, defaultMathValue, defaultMathValue,
                true, 0.25, false,
                -1, -1, -1, -1, -1, -1, -1, -1));

        enemiesPathParams.put(EnemyType.S17, paramType5);
        enemiesPathParams.put(EnemyType.S18, paramType5);
        enemiesPathParams.put(EnemyType.S19, paramType5);
        enemiesPathParams.put(EnemyType.S20, paramType5);
        enemiesPathParams.put(EnemyType.S21, paramType5);

        enemiesPathParams.put(EnemyType.S22, new PathParam(MobOrientation.Down_45, false, defaultMathValue, defaultMathValue,
                true, 0.05, true,
                30, 35, 30, 35, 15, 15, 15, 15));

        enemiesPathParams.put(EnemyType.S23, new PathParam(MobOrientation.Vert, false, defaultMathValue, defaultMathValue,
                true, 0.02, true,
                15, 35, 15, 35, 15, 15, 15, 15));

        enemiesPathParams.put(EnemyType.S24, new PathParam(MobOrientation.Vert, false, defaultMathValue, defaultMathValue,
                true, 0.15, false,
                -1, -1, -1, -1, -1, -1, -1, -1));

        enemiesPathParams.put(EnemyType.S25, new PathParam(MobOrientation.Vert, false, defaultMathValue, defaultMathValue,
                true, 0.12, false,
                -1, -1, -1, -1, -1, -1, -1, -1));

        enemiesPathParams.put(EnemyType.S26, paramType6);
        enemiesPathParams.put(EnemyType.S27, paramType6);
        enemiesPathParams.put(EnemyType.S28, paramType6);

        enemiesPathParams.put(EnemyType.S29, new PathParam(MobOrientation.Vert, false, defaultMathValue, defaultMathValue,
                true, 0.15, false,
                -1, -1, -1, -1, -1, -1, -1, -1));

        enemiesPathParams.put(EnemyType.S30, new PathParam(MobOrientation.Vert, false, defaultMathValue, defaultMathValue,
                true, 0.25, false,
                -1, -1, -1, -1, -1, -1, -1, -1));

        enemiesPathParams.put(EnemyType.S31, new PathParam(MobOrientation.Vert, false, defaultMathValue, defaultMathValue,
                true, 0.25, false,
                -1, -1, -1, -1, -1, -1, -1, -1));

        enemiesPathParams.put(EnemyType.BOSS, new PathParam(MobOrientation.Vert, false, 9, 9,
                true, 0.1, false,
                -1, -1, -1, -1, -1, -1, -1, -1));

    }

    private MathData() {
    }

    public static int getFixedBossPay(GameConfig config, BossType bossType) {
        return config.getBosses().get(bossType.getSkinId()).getFixedPay();
    }

    public static Set<Integer> getPossibleBetLevels() {
        return possibleBetLevels;
    }

    public static Integer getPaidWeaponCost(GameConfig config, int weaponId) {
        return config.getWeaponPrices().get(weaponId);
    }

    public static int getEnemyPayout(GameConfig config, EnemyType enemyType, int idx) {
        return config.getEnemyData(enemyType).getPay();
    }

    public static int getMinEnemyPayout(GameConfig config, EnemyType enemyType, int idx) {
        return config.getEnemyData(enemyType).getMinPay();
    }

    public static int getMaxEnemyPayout(GameConfig config, EnemyType enemyType, int idx) {
        return config.getEnemyData(enemyType).getMaxPay();
    }

    public static BossType getRandomBossType(GameConfig config) {
        Map<Integer, Double> weights = new HashMap<>();
        config.getBosses().forEach((skinId, bossParams) -> {
            weights.put(skinId, (double) bossParams.getPickWeight());
        });
        return BossType.getBySkinId(GameTools.getRandomNumberKeyFromMapWithNorm(weights));
    }

    public static <T extends IWeight> T getRandomElementFromWeightedList(List<T> list) {
        double[] weights = list.stream().mapToDouble(T::getWeight).toArray();
        double sum = Arrays.stream(weights).sum();
        if (sum != 1) {
            weights = Arrays.stream(weights).map(v -> v / sum).toArray();
        }
        return list.get(GameTools.getIndexFromDoubleProb(weights));
    }


    private static double calculateSumOfEuclideanDistances(Trajectory currentTrajectory,
                                                           List<Pair<EnemyType, Trajectory>> enemyTrajectoryPairs) {
        double sum = 0;
        double currentX = currentTrajectory.getPoints().get(0).getX();
        double currentY = currentTrajectory.getPoints().get(0).getY();
        for (Pair<EnemyType, Trajectory> pair : enemyTrajectoryPairs) {
            double x = pair.getValue().getPoints().get(0).getX();
            double y = pair.getValue().getPoints().get(0).getY();
            sum += Math.pow((currentX - x), 2) + Math.pow((currentY - y), 2);
        }
        return sum;
    }


    public static PathParam getPathParamByEnemy(EnemyType enemyType) {
        return enemiesPathParams.get(enemyType);
    }
}
