package com.betsoft.casino.mp.bgdragonstone.model.math;

import com.betsoft.casino.mp.bgdragonstone.model.math.config.GameConfig;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.dgphoenix.casino.common.util.RNG;
import java.util.*;

public class MathData {
    public static final int TURRET_WEAPON_ID = -1;
    private static final Set<Integer> possibleBetLevels;

    static {
        possibleBetLevels = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(3, 5, 10)));
    }

    public static Integer getRandomDamageForWeapon(GameConfig config, Integer weaponId) {
        return GameTools.getRandomNumberKeyFromMapWithNorm(config.getWeaponTargets(weaponId));
    }

    public static Integer getRandomMultForWeapon(GameConfig config, Integer weaponId) {
        return GameTools.getRandomNumberKeyFromMapWithNorm(config.getCriticalHitMultipliers(weaponId));
    }

    public static int getRandomBossSmallPay(GameConfig config) {
        return GameTools.getRandomNumberKeyFromMapWithNorm(config.getBoss().getPartialPays());
    }

    public static double getHitSmallProbabilityForBoss(GameConfig config, int weaponId, int randomBossPay) {
        return  config.getBoss().getPartialPayProb().get(weaponId).get(randomBossPay);
    }


    public static Set<Integer> getPossibleBetLevels() {
        return possibleBetLevels;
    }

    public static Integer getPaidWeaponCost(GameConfig config, int weaponId) {
        return config.getWeaponPrices().get(weaponId);
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



    public static int  getRandomRagePayouts(GameConfig config) {
        return RNG.nextInt(config.getRageMin(), config.getRageMax() + 1);
    }

    public static int  getRandomSpiritPayouts(GameConfig config) {
        return RNG.nextInt(config.getSpiritMin(), config.getSpiritMax() + 1);
    }

}
