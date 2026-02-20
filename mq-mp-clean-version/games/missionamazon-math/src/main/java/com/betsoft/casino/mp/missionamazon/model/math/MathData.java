package com.betsoft.casino.mp.missionamazon.model.math;

import com.betsoft.casino.mp.missionamazon.model.math.config.BossPays;
import com.betsoft.casino.mp.missionamazon.model.math.config.BossShots;
import com.betsoft.casino.mp.missionamazon.model.math.config.GameConfig;
import com.betsoft.casino.mp.missionamazon.model.math.config.WeaponDrop;
import com.betsoft.casino.mp.model.Gem;
import com.betsoft.casino.mp.model.IWeight;
import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.model.movement.MathUtils;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointD;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

public class MathData {
    public static final int TURRET_WEAPON_ID = -1;
    private static final Set<Integer> possibleBetLevels =
            Collections.unmodifiableSet(new TreeSet<>(Arrays.asList(1, 2, 3, 5, 10)));

    private static final Map<Integer, Double> WEAPON_QUEST_TARGET_EN = buildWeaponQuestTargetEn();
    private static final Map<Integer, Double> WEAPON_QUEST_TARGET_BOSS = buildWeaponQuestTargetBoss();
    private static final Map<Integer, Double> WEAPON_CONTRIB_RTP_TO_WD_BASE = buildWeaponContribRTPToWDBase();
    private static final Map<Integer, Double> WEAPON_CONTRIB_RTP_TO_WD_BOSS = buildWeaponContribRTPToWDBoss();

    private static final Map<Integer, Double> WEAPON_CONTRIB_COMPENSATE = buildWeaponContribCompencate();


    private static Map<Integer, Double> buildWeaponQuestTargetEn() {
        Map<Integer, Double> weaponQuestTargetEn = new HashMap<>();
        weaponQuestTargetEn.put(7, 0.25);
        weaponQuestTargetEn.put(9, 0.28);
        weaponQuestTargetEn.put(10, 0.34);
        weaponQuestTargetEn.put(3, 0.40);
        weaponQuestTargetEn.put(4, 0.48);
        weaponQuestTargetEn.put(-1, 3.0);
        return weaponQuestTargetEn;
    }

    private static Map<Integer, Double> buildWeaponQuestTargetBoss() {
        Map<Integer, Double> weaponQuestTargetBoss = new HashMap<>();
        weaponQuestTargetBoss.put(7, 0.30);
        weaponQuestTargetBoss.put(9, 0.35);
        weaponQuestTargetBoss.put(10, 0.40);
        weaponQuestTargetBoss.put(3, 0.48);
        weaponQuestTargetBoss.put(4, 0.58);
        weaponQuestTargetBoss.put(-1, 4.0);
        return weaponQuestTargetBoss;
    }

    private static Map<Integer, Double> buildWeaponContribRTPToWDBase() {
        Map<Integer, Double> temp = new HashMap<>();
        temp.put(SpecialWeaponType.ArtilleryStrike.getId(), 1.51643277501132);
        temp.put(SpecialWeaponType.Flamethrower.getId(), 1.35861733203505);
        temp.put(SpecialWeaponType.Cryogun.getId(), 1.3570802919708);
        temp.put(SpecialWeaponType.Ricochet.getId(), 1.35620437956204);
        temp.put(SpecialWeaponType.Plasma.getId(),  1.91875968992248);
        return temp;
    }

    private static Map<Integer, Double> buildWeaponContribRTPToWDBoss() {
        Map<Integer, Double> temp = new HashMap<>();
        temp.put(SpecialWeaponType.ArtilleryStrike.getId(), 1.78021041557075);
        temp.put(SpecialWeaponType.Flamethrower.getId(), 1.84456806282723);
        temp.put(SpecialWeaponType.Cryogun.getId(),  1.69954751131222);
        temp.put(SpecialWeaponType.Ricochet.getId(),  1.85424901185771);
        temp.put(SpecialWeaponType.Plasma.getId(),  1.99404255319149);
        return temp;
    }


    private static Map<Integer, Double> buildWeaponContribCompencate() {
        Map<Integer, Double> temp = new HashMap<>();
        temp.put(SpecialWeaponType.ArtilleryStrike.getId(), 16.749);
        temp.put(SpecialWeaponType.Flamethrower.getId(), 13.953);
        temp.put(SpecialWeaponType.Cryogun.getId(), 11.1552);
        temp.put(SpecialWeaponType.Ricochet.getId(), 9.29);
        temp.put(SpecialWeaponType.Plasma.getId(),   7.4256);
        return temp;
    }

    private MathData() {
    }

    public static double calculateAverageDropPrice(GameConfig config, boolean isBoss) {
        List<WeaponDrop> weaponDrops = isBoss ? config.getWeaponDropsTargetBoss() : config.getWeaponDropsTargetEn();
        double sum = weaponDrops.stream().mapToDouble(WeaponDrop::getWeight).sum();
        double avg = weaponDrops.stream()
                .mapToDouble(drop -> config.getWeaponPrice(drop.getType()) * drop.getAmount() * drop.getWeight() / sum)
                .sum();
        return config.getGameRTP() * avg / 100;
    }

    public static Integer getRandomDamageForWeapon(GameConfig config, Integer weaponId, boolean isBoss) {
        Map<Integer, Double> targets = isBoss ? config.getWeaponTargetsBoss(weaponId) : config.getWeaponTargetsEn(weaponId);
        return GameTools.getRandomNumberKeyFromMapWithNorm(targets);
    }

    public static Integer getRandomMultForWeapon(GameConfig config, Integer weaponId, boolean isBoss) {
        return GameTools.getRandomNumberKeyFromMapWithNorm(isBoss ? config.getCriticalHitMultipliersTargetBoss(weaponId) : config.getCriticalHitMultipliersTargetEn(weaponId));
    }

    public static int getRandomNumberShotsToBoss(GameConfig config, Integer weaponId, boolean isBoss) {
        Map<Integer, BossShots> shoots = isBoss ? config.getBossParams().getPercentNumShotsSWOnBossTargetBoss() : config.getBossParams().getPercentNumShotsSWOnBossTargetEn();
        return RNG.nextInt((int) (shoots.get(weaponId).getMin() * 100), (int) (shoots.get(weaponId).getMax() * 100));
    }

    public static double getAverageNumberOfEnemiesForWeapon(GameConfig config, int weaponTypeId) {
        return MathUtils.sumProduct(config.getWeaponTargetsEn(weaponTypeId)) / 100;
    }

    public static Double getAverageDamageForWeapon(GameConfig config, Integer weaponId) {
        return getAverageNumberOfEnemiesForWeapon(config, weaponId);
    }

    public static Double getAverageDamageForWeapon(GameConfig config, Integer weaponId, boolean isBossTarget) {
        return isBossTarget ? MathUtils.sumProduct(config.getWeaponTargetsBoss(weaponId)) / 100 : MathUtils.sumProduct(config.getWeaponTargetsEn(weaponId)) / 100;
    }

    public static Double getRtpForDropWeapon(GameConfig config, EnemyType enemyType, int weaponId, int idx, boolean isBoss) {
        if (weaponId == -1) {
            return config.getEnemyData(enemyType, idx).getBaseTurretWeaponRTP() / 100;
        }
        return (isBoss ? config.getWeaponDropSpecialWeaponRTPTargetBoss().get(weaponId) : config.getWeaponDropSpecialWeaponRTPTargetEn().get(weaponId)) / 100;
    }

    public static Double getFullRtpForWeapon(GameConfig config, int weaponId, boolean isBossTarget) {
        double averageTargets = (isBossTarget ? MathUtils.sumProduct(config.getWeaponTargetsBoss(weaponId)) : MathUtils.sumProduct(config.getWeaponTargetsEn(weaponId))) / 100;
        double questRTP = isBossTarget ? WEAPON_QUEST_TARGET_BOSS.get(weaponId) : WEAPON_QUEST_TARGET_EN.get(weaponId);
        return config.getWeaponPrice(weaponId) * (config.getGameRTP() - questRTP) / averageTargets / 100;
    }

    public static Double getCompensationSW(GameConfig config, int weaponId) {
        return ((double) config.getWeaponPrice(weaponId) / 100) * config.getGameRTP();
    }

    public static int getRandomBossSmallPay(GameConfig config, BossType bossType, int weaponTypeId) {
        return GameTools.getRandomNumberKeyFromMapWithNorm(
                config.getBossParams().getBossPaysByType(bossType).getPartialPaysWeights(weaponTypeId));
    }

    public static double getHitSmallProbabilityForBoss(GameConfig config, BossType bossType,
                                                       int weaponId, int randomBossPay, boolean isBoss) {
        BossPays bossPays = config.getBossParams().getBossPaysByType(bossType);
        Map<Integer, Map<Integer, Double>> partialPayProb = isBoss ? bossPays.getPartialPaysProbTargetBoss() : bossPays.getPartialPaysProbTargetEn();
        return partialPayProb.get(weaponId).get(randomBossPay);
    }

    public static int getRandomDefeatBossMultiplier(GameConfig config) {
        return GameTools.getRandomNumberKeyFromMapWithNorm(config.getBossParams().getDefeatMultiplier());
    }

    public static Set<Integer> getPossibleBetLevels() {
        return possibleBetLevels;
    }

    public static Integer getPaidWeaponCost(GameConfig config, int weaponId) {
        return config.getWeaponPrices().get(weaponId);
    }

    public static double calculateAveragePayWithoutWeapons(GameConfig config, int weaponCost,
                                                           double averageTargets, int weaponId) {
        return (weaponCost * config.getGameRTP()
                - config.getWeaponDropSpecialWeaponRTPTargetEn().get(weaponId)) / averageTargets / 100;
    }

    public static double getHitProbability(GameConfig config, int weaponTypeId, EnemyType enemyType, int idx, boolean isBoss) {
        return getHitProbability(weaponTypeId, config.getEnemyData(enemyType, idx), isBoss);
    }

    private static double getHitProbability(int weaponTypeId, EnemyData enemyData, boolean isBoss) {
        return isBoss ? enemyData.getKillProbabilitiesTargetBoss(weaponTypeId) : enemyData.getKillProbabilitiesTargetEn(weaponTypeId);
    }

    public static int getEnemyPayout(GameConfig config, EnemyType enemyType, int idx) {
        return config.getEnemyData(enemyType, idx).getPayout();
    }

    public static BossType getRandomBossType(GameConfig config) {
        return BossType.getBySkinId(GameTools.getRandomNumberKeyFromMapWithNorm(
                config.getBossParams().getPickBossProbabilities()));
    }

    public static double getGemDropProbability(GameConfig config, int weaponId, boolean isBoss) {
        Map<Integer, Double> dropProbability = isBoss ? config.getQuestParams().getDropProbabilityByWeaponTargetBoss() :
                config.getQuestParams().getDropProbabilityByWeaponTargetEn();
        return dropProbability.get(weaponId);
    }

    public static Gem getGemByBetLevel(int betLevel) {
        List<Integer> list = new ArrayList<>(possibleBetLevels);
        list.sort(Comparator.naturalOrder());
        int gemId = list.indexOf(betLevel);
        return Gem.getById(gemId);
    }

    public static long getGemPrize(GameConfig gameConfig) {
        int minPrize = gameConfig.getQuestParams().getMinPrize();
        int maxPrize = gameConfig.getQuestParams().getMaxPrize();
        return Math.round(minPrize + ((maxPrize - minPrize) * Math.pow(RNG.rand(), 2)));
    }

    public static WeaponDrop getRandomWeaponDrop(GameConfig config, boolean isBoss) {
        return isBoss ? getRandomElementFromWeightedList(config.getWeaponDropsTargetBoss()) : getRandomElementFromWeightedList(config.getWeaponDropsTargetEn());
    }

    private static <T extends IWeight> T getRandomElementFromWeightedList(List<T> list) {
        double[] weights = list.stream().mapToDouble(T::getWeight).toArray();
        double sum = Arrays.stream(weights).sum();
        if (sum != 1) {
            weights = Arrays.stream(weights).map(v -> v / sum).toArray();
        }
        return list.get(GameTools.getIndexFromDoubleProb(weights));
    }

    public static WeaponDrop getWeaponCarrierDrop(GameConfig config, EnemyType weaponCarrier, boolean isBoss) {
        Map<EnemyType, List<WeaponDrop>> weaponCarrierDrops = isBoss ? config.getWeaponCarrierDropsTargetBoss() : config.getWeaponCarrierDropsTargetEn();
        return getRandomElementFromWeightedList(weaponCarrierDrops.get(weaponCarrier));
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

    public static Money getFullCompensationForWeapon(GameConfig config, Money stake, int weaponId, int count, int countWC, int betLevel){
        Money result = Money.ZERO;
        if(weaponId == -1){
            return Money.ZERO;
        }

        double questRTP = WEAPON_QUEST_TARGET_EN.get(weaponId);
        double rtpForWeapon = ((double) config.getWeaponPrice(weaponId) / 100) * (config.getGameRTP() - questRTP);
        double rtpWithoutContrib = WEAPON_CONTRIB_COMPENSATE.get(weaponId);

        if(countWC > 0){
            double multiplier = new BigDecimal(countWC, MathContext.DECIMAL32)
                    .multiply(new BigDecimal(rtpWithoutContrib, MathContext.DECIMAL32))
                    .multiply(new BigDecimal(betLevel, MathContext.DECIMAL32))
                    .doubleValue();
            result = result.add(stake.getWithMultiplier(multiplier));
        }

        if(count > 0){
            double multiplier = new BigDecimal(count, MathContext.DECIMAL32)
                    .multiply(new BigDecimal(rtpForWeapon, MathContext.DECIMAL32))
                    .multiply(new BigDecimal(betLevel, MathContext.DECIMAL32))
                    .doubleValue();
            result = result.add(stake.getWithMultiplier(multiplier));
        }
        return result;
    }

    public static double getContribToWDForWeapon(int weaponId, boolean isBoss){
        if(isBoss) {
            return WEAPON_CONTRIB_RTP_TO_WD_BOSS.containsKey(weaponId) ? WEAPON_CONTRIB_RTP_TO_WD_BOSS.get(weaponId) : 0;
        }else{
            return WEAPON_CONTRIB_RTP_TO_WD_BASE.containsKey(weaponId) ? WEAPON_CONTRIB_RTP_TO_WD_BASE.get(weaponId) : 0;
        }
    }
}
