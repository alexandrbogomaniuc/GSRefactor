package com.betsoft.casino.mp.dragonstone.model.math;

import com.betsoft.casino.mp.dragonstone.model.math.config.BossParams;
import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfig;
import com.betsoft.casino.mp.dragonstone.model.math.config.WeaponDrop;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.model.movement.MathUtils;
import com.dgphoenix.casino.common.util.RNG;

import java.util.*;

public class MathData {
    public static final int TURRET_WEAPON_ID = -1;
    private static final Set<Integer> possibleBetLevels =
            Collections.unmodifiableSet(new TreeSet<>(Arrays.asList(1, 2, 3, 5, 10)));

    public static double calculateAverageDropPrice(GameConfig config) {
        List<WeaponDrop> weaponDrops = config.getWeaponDrops();
        double sum = weaponDrops.stream().mapToDouble(WeaponDrop::getWeight).sum();
        double avg = weaponDrops.stream()
                .mapToDouble(drop -> config.getWeaponPrice(drop.getType()) * drop.getAmount() * drop.getWeight() / sum)
                .sum();
        return config.getGameRTP() * avg / 100;
    }

    public static double calculateFullWeaponRTP(GameConfig config, int weaponId) {
        double averageTargets = getAverageNumberOfEnemiesForWeapon(config, weaponId);
        return config.getWeaponPrice(weaponId) * config.getGameRTP() / averageTargets / 100;
    }

    public static double calculateWeaponRTP(GameConfig config, int weaponId) {
        double averageTargets = getAverageNumberOfEnemiesForWeapon(config, weaponId);
        return calculateAveragePayWithoutSlotAndWeapons(config, getPaidWeaponCost(config, weaponId), averageTargets, weaponId);
    }

    private static double getAverageWeaponMult(GameConfig config, int weaponId) {
        Map<Integer, Double> weaponMults = config.getCriticalHitMultipliers(weaponId);
        return MathUtils.sumProduct(weaponMults) / 100;
    }

    public static Integer getRandomDamageForWeapon(GameConfig config, Integer weaponId) {
        return GameTools.getRandomNumberKeyFromMapWithNorm(config.getWeaponTargets(weaponId));
    }

    public static Integer getRandomMultForWeapon(GameConfig config, Integer weaponId) {
        return GameTools.getRandomNumberKeyFromMapWithNorm(config.getCriticalHitMultipliers(weaponId));
    }

    public static double getAverageNumberOfEnemiesForWeapon(GameConfig config, int weaponTypeId) {
        return MathUtils.sumProduct(config.getWeaponTargets(weaponTypeId)) / 100;
    }

    public static Double getAverageDamageForWeapon(GameConfig config, Integer weaponId) {
        return getAverageNumberOfEnemiesForWeapon(config, weaponId);
    }

    public static Double getRtpForDropWeapon(GameConfig config, EnemyType enemyType, int weaponId, int idx) {
        if (weaponId == -1) {
            return config.getEnemyData(enemyType, idx).getBaseTurretWeaponRTP() / 100;
        }
        return config.getWeaponDropSpecialWeaponRTP().get(weaponId) / 100;
    }

    public static Double getFullRtpForWeapon(GameConfig config, int weaponId) {
        return calculateFullWeaponRTP(config, weaponId);
    }

    public static int getRandomBossSmallPay(GameConfig config) {
        return GameTools.getRandomNumberKeyFromMapWithNorm(config.getBoss().getSmallPays());
    }

    public static double getHitSmallProbabilityForBoss(GameConfig config, int weaponId) {
        BossParams boss = config.getBoss();
        double fullWeaponRTP = calculateFullWeaponRTP(config, weaponId);
        double bossSmallTurretRtp = getBossSmallTurretRtp(config);
        double averageSmallPay = boss.getAverageSmallPay();
        return weaponId == -1
                ? bossSmallTurretRtp / averageSmallPay
                : fullWeaponRTP * bossSmallTurretRtp / (config.getGameRTP() / 100) / averageSmallPay;
    }

    public static double getHitKillProbabilityForBoss(GameConfig config, int weaponId) {
        BossParams boss = config.getBoss();
        return weaponId == -1
                ? boss.getKilledTurretRTP() / boss.getKilledPay()
                : calculateFullWeaponRTP(config, weaponId) * boss.getKilledTurretRTP() / (config.getGameRTP() / 100) / boss.getKilledPay();
    }


    public static Set<Integer> getPossibleBetLevels() {
        return possibleBetLevels;
    }

    public static Integer getPaidWeaponCost(GameConfig config, int weaponId) {
        return config.getWeaponPrices().get(weaponId);
    }

    public static double calculateAveragePayWithoutSlotAndWeapons(GameConfig config, int weaponCost,
                                                                  double averageTargets, int weaponId) {
        return (weaponCost * config.getGameRTP() - config.getSlotContribution().get(weaponId)
                - config.getWeaponDropSpecialWeaponRTP().get(weaponId)) / averageTargets / 100;
    }

    public static double getSpecterKillProbability(GameConfig config, EnemyType specterType, int weaponId, int idx) {
        EnemyData data = config.getEnemyData(specterType, idx);
        double weaponRTP = weaponId == -1
                ? data.getBaseTurretRTP() / 100
                : calculateWeaponRTP(config, weaponId);
        int specterPay = getSpecterPay(config, specterType, idx);
        return weaponRTP / specterPay;
    }

    public static int getSpecterPay(GameConfig config, EnemyType specterType, int idx) {
        if (config.getEnemyData(specterType, idx).isConfiguredWithMinMaxPay()) {
            return RNG.nextInt(config.getEnemyData(specterType, idx).getMinPay(),
                    config.getEnemyData(specterType, idx).getMaxPay() + 1);
        } else {
            return config.getEnemyData(specterType, idx).getPayout();
        }
    }

    public static int[] getDistributionByEnemy(int totalDamage, int cntEnemies) {
        int averageDamage = (totalDamage / cntEnemies / 10) * 10;
        int sumDamage = 0;
        int[] res = new int[cntEnemies];
        for (int i = 0; i < res.length - 1; i++) {
            res[i] = averageDamage;
            sumDamage += averageDamage;
        }
        res[cntEnemies - 1] = totalDamage - sumDamage;
        return res;
    }

    public static double getHitProbability(GameConfig config, int weaponTypeId, EnemyType enemyType, int idx) {
        return getHitProbability(weaponTypeId, config.getEnemyData(enemyType, idx));
    }

    public static double getHitProbability(int weaponTypeId, EnemyData enemyData) {
        return enemyData.getKillProbability(weaponTypeId);
    }

    public static int getEnemyPayout(GameConfig config, EnemyType enemyType, int weaponTypeId, int idx) {
        return config.getEnemyData(enemyType, idx).getPayout();
    }

    public static double getBossSmallTurretRtp(GameConfig config) {
        return config.getGameRTP() / 100 - config.getBoss().getKilledTurretRTP();
    }

    public static int getRandomRagePayouts(GameConfig config) {
        return RNG.nextInt(config.getRageMin(), config.getRageMax() + 1);
    }

    public static int getRandomSpiritPayouts(GameConfig config) {
        return RNG.nextInt(config.getSpiritMin(), config.getSpiritMax() + 1);
    }

}
