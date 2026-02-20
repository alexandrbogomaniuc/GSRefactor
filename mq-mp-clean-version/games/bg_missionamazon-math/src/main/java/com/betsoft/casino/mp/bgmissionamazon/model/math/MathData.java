package com.betsoft.casino.mp.bgmissionamazon.model.math;

import com.betsoft.casino.mp.bgmissionamazon.model.math.config.*;
import com.betsoft.casino.mp.model.IWeight;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.*;

public class MathData {
    public static final int TURRET_WEAPON_ID = -1;
    private static final Set<Integer> possibleBetLevels = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(3, 5, 10)));
    private static final Map<SpecialWeaponType, SpecialWeaponType> weaponsDependency;

    static {
        weaponsDependency = new HashMap<>();
        weaponsDependency.put(SpecialWeaponType.PowerUp_ArtilleryStrike, SpecialWeaponType.ArtilleryStrike);
        weaponsDependency.put(SpecialWeaponType.PowerUp_Cryogun, SpecialWeaponType.Cryogun);
        weaponsDependency.put(SpecialWeaponType.PowerUp_Flamethrower, SpecialWeaponType.Flamethrower);
        weaponsDependency.put(SpecialWeaponType.PowerUp_Laser, SpecialWeaponType.Ricochet);
        weaponsDependency.put(SpecialWeaponType.PowerUp_Plasma, SpecialWeaponType.Plasma);
    }

    private MathData() {
    }

    public static Integer getRandomDamageForWeapon(GameConfig config, SpecialWeaponType weaponType, boolean isBossTarget) {
        boolean isSW = weaponType != null;
        if (isSW) {
            Map<SpecialWeaponType, Map<Integer, Double>> weaponTargets = isBossTarget ? config.getWeaponTargetsBoss() : config.getWeaponTargetsEn();
            SpecialWeaponType realWeaponType = weaponTargets.containsKey(weaponType) ? weaponType : weaponsDependency.get(weaponType);
            return GameTools.getRandomNumberKeyFromMapWithNorm(weaponTargets.get(realWeaponType));
        }
        return GameTools.getRandomNumberKeyFromMapWithNorm(isBossTarget ? config.getTurretTargetsBoss() : config.getTurretTargetsEn());
    }

    public static Integer getRandomMultForWeapon(GameConfig config, SpecialWeaponType weaponType, boolean isBossTarget) {
        if (weaponType == null) {
            Map<Integer, Double> chMultipliersTurret = isBossTarget ? config.getCriticalHitTurretMultipliersTargetBoss() :
                    config.getCriticalHitTurretMultipliersTargetEn();
            return GameTools.getRandomNumberKeyFromMapWithNorm(chMultipliersTurret);
        }
        Map<SpecialWeaponType, Map<Integer, Double>> criticalHitMultipliers = isBossTarget ? config.getCriticalHitMultipliersTargetBoss() :
                config.getCriticalHitMultipliersTargetEn();
        SpecialWeaponType realWeaponType = criticalHitMultipliers.containsKey(weaponType) ? weaponType : weaponsDependency.get(weaponType);
        return GameTools.getRandomNumberKeyFromMapWithNorm(criticalHitMultipliers.get(realWeaponType));
    }

    public static int getRandomBossSmallPay(GameConfig config, SpecialWeaponType weaponType) {
        BossPays bossPaysByType = config.getBossParams().getBossPays();
        if (weaponType == null) {
            return GameTools.getRandomNumberKeyFromMapWithNorm(bossPaysByType.getPartialPaysWeightsTurret());
        }
        SpecialWeaponType realWeaponType = bossPaysByType.getPartialPaysWeights().containsKey(weaponType) ? weaponType : weaponsDependency.get(weaponType);
        return GameTools.getRandomNumberKeyFromMapWithNorm(bossPaysByType.getPartialPaysWeights().get(realWeaponType));
    }

    public static double getHitSmallProbabilityForBoss(GameConfig config, SpecialWeaponType weaponType, int randomBossPay, boolean isBossTarget) {
        BossPays bossPaysByType = config.getBossParams().getBossPays();
        if (weaponType == null) {
            return bossPaysByType.getPartialPaysProbTurretTargetBoss().get(randomBossPay);
        }
        Map<SpecialWeaponType, Map<Integer, Double>> partialPaysProb = isBossTarget ? bossPaysByType.getPartialPaysProbTargetBoss() : bossPaysByType.getPartialPaysProbTargetEn();
        SpecialWeaponType realWeaponType = partialPaysProb.containsKey(weaponType) ? weaponType : weaponsDependency.get(weaponType);
        return partialPaysProb.get(realWeaponType).get(randomBossPay);
    }

    public static int getRandomDefeatBossMultiplier(GameConfig config) {
        return GameTools.getRandomNumberKeyFromMapWithNorm(config.getBossParams().getDefeatMultiplier());
    }

    public static Set<Integer> getPossibleBetLevels() {
        return possibleBetLevels;
    }

    public static double getHitProbability(GameConfig config, SpecialWeaponType weaponType, EnemyType enemyType, int idx, boolean isBossTarget) {
        return getHitProbability(weaponType, config.getEnemyData(enemyType, idx), isBossTarget);
    }

    public static double getHitProbability(SpecialWeaponType weaponType, EnemyData enemyData, boolean isBossTarget) {
        if (weaponType == null) {
            return enemyData.getBaseTurretKillProb();
        }
        Map<SpecialWeaponType, Double> killProbabilities = isBossTarget ? enemyData.getKillProbabilitiesTargetBoss() : enemyData.getKillProbabilitiesTargetEn();

        SpecialWeaponType realWeaponType = killProbabilities.containsKey(weaponType) ? weaponType : weaponsDependency.get(weaponType);
        return killProbabilities.get(realWeaponType);
    }

    public static int getEnemyPayout(GameConfig config, EnemyType enemyType, int idx) {
        return config.getEnemyData(enemyType, idx).getPayout();
    }

    public static BossType getRandomBossType(GameConfig config) {
        return BossType.getBySkinId(GameTools.getRandomNumberKeyFromMapWithNorm(
                config.getBossParams().getPickBossProbabilities()));
    }

    public static double getGemDropProbability(GameConfig config, int weaponId, boolean isBossTarget) {
        Map<Integer, Double> dropProbability = isBossTarget ? config.getQuestParams().getDropProbabilityByWeaponTargetBoss() :
                config.getQuestParams().getDropProbabilityByWeaponTargetEn();
        return dropProbability.get(weaponId);
    }

    public static GemDrop getRandomGemDrop(GameConfig config) {
        return getRandomElementFromWeightedList(config.getQuestParams().getGemDrops());
    }

    public static WeaponDrop getRandomWeaponDrop(GameConfig config, int weaponId, boolean isBossTarget) {
        if (weaponId == -1) {
            List<WeaponDrop> turretDrops = isBossTarget ? config.getWeaponDropsATargetBoss() : config.getWeaponDropsATargetEn();
            return getRandomElementFromWeightedList(turretDrops);
        }
        List<WeaponDrop> weaponDrops = isBossTarget ? config.getWeaponDropsBTargetBoss() : config.getWeaponDropsBTargetEn();
        return getRandomElementFromWeightedList(weaponDrops);
    }

    private static <T extends IWeight> T getRandomElementFromWeightedList(List<T> list) {
        double[] weights = list.stream().mapToDouble(T::getWeight).toArray();
        double sum = Arrays.stream(weights).sum();
        if (sum != 1) {
            weights = Arrays.stream(weights).map(v -> v / sum).toArray();
        }
        return list.get(GameTools.getIndexFromDoubleProb(weights));
    }

    public static WeaponDrop getWeaponCarrierDrop(GameConfig config, EnemyType weaponCarrier, boolean isBossTarget) {
        Map<EnemyType, List<WeaponDrop>> WCDrops = isBossTarget ? config.getWeaponCarrierDropsTargetBoss() : config.getWeaponCarrierDropsTargetEn();
        return getRandomElementFromWeightedList(WCDrops.get(weaponCarrier));
    }

    public static Map<Pair<EnemyType, Trajectory>, Double> calculateInitialSpawnWeights(
            List<Pair<EnemyType, Trajectory>> enemyTrajectoryPairs, Map<EnemyRange, double[]> initialWeightsFromConfig, int[] dividersFromConfig) {
        Map<Pair<EnemyType, Trajectory>, Double> spawnWeights = new HashMap<>();
        enemyTrajectoryPairs.forEach(pair -> {
            double spawnWeight = SpawnStage.getInitialWeightForEnemy(pair.getKey(),
                    calculateSumOfEuclideanDistances(pair.getValue(), enemyTrajectoryPairs), initialWeightsFromConfig, dividersFromConfig);
            spawnWeights.put(pair, spawnWeight);
        });
        return spawnWeights;
    }

    public static Map<Pair<EnemyType, Trajectory>, Double> calculateCurrentSpawnWeights(
            List<Pair<EnemyType, Trajectory>> enemyTrajectoryPairs, List<PointD> currentEnemiesLocations,
            long startRoundTime, List<SpawnStageFromConfig> spawnStages) {
        double meanDistance = enemyTrajectoryPairs.stream().mapToDouble(pair ->
                calculateSumOfEuclideanDistancesByLocations(pair.getValue(), currentEnemiesLocations)).sum();
        Map<Pair<EnemyType, Trajectory>, Double> spawnWeights = new HashMap<>();
        enemyTrajectoryPairs.forEach(pair -> spawnWeights.put(pair,
                SpawnStage.getWeightForEnemyByTime(pair.getKey(), startRoundTime, spawnStages) * meanDistance));
        return spawnWeights;
    }

    public static Map<PointD, Double> calculateStaticEnemiesSpawnWeights(
            List<PointD> staticPoints, List<PointD> currentEnemiesLocations) {
        Map<PointD, Double> spawnWeights = new HashMap<>();
        staticPoints.forEach(point ->
                spawnWeights.put(point, calculateSumOfEuclideanDistancesForStaticts(point, currentEnemiesLocations)));
        return spawnWeights;
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

    private static double calculateSumOfEuclideanDistancesByLocations(Trajectory currentTrajectory, List<PointD> enemyLocations) {
        double sum = 0;
        double currentX = currentTrajectory.getPoints().get(0).getX();
        double currentY = currentTrajectory.getPoints().get(0).getY();
        for (PointD point : enemyLocations) {
            sum += Math.pow((currentX - point.x), 2) + Math.pow((currentY - point.y), 2);
        }
        return sum;
    }

    private static double calculateSumOfEuclideanDistancesForStaticts(PointD staticPoint, List<PointD> enemyLocations) {
        double sum = 0;
        double currentX = staticPoint.x;
        double currentY = staticPoint.y;
        for (PointD point : enemyLocations) {
            sum += Math.pow((currentX - point.x), 2) + Math.pow((currentY - point.y), 2);
        }
        return sum;
    }

    public static SpecialWeaponType getBaseSpecialWeaponType(SpecialWeaponType weaponType) {
        return weaponsDependency.getOrDefault(weaponType, weaponType);
    }

    public static int getRandomNumberShotsToBoss(GameConfig config, SpecialWeaponType weaponType, boolean isBossTarget) {
        Map<SpecialWeaponType, BossShots> shoots = isBossTarget ? config.getBossParams().getPercentNumShotsSWOnBossTargetBoss() : config.getBossParams().getPercentNumShotsSWOnBossTargetEn();
        SpecialWeaponType realWeaponType = shoots.containsKey(weaponType) ? weaponType : weaponsDependency.get(weaponType);
        return RNG.nextInt((int) (shoots.get(realWeaponType).getMin() * 100), (int) (shoots.get(realWeaponType).getMax() * 100));
    }

}
