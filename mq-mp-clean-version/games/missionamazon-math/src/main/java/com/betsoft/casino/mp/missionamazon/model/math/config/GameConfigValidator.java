package com.betsoft.casino.mp.missionamazon.model.math.config;

import com.betsoft.casino.mp.missionamazon.model.math.BossType;
import com.betsoft.casino.mp.missionamazon.model.math.EnemyData;
import com.betsoft.casino.mp.missionamazon.model.math.EnemyRange;
import com.betsoft.casino.mp.missionamazon.model.math.EnemyType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GameConfigValidator {
    private static final double EPS = 0.0000001;
    private static final int[] weapons = {-1, 7, 9, 10, 3, 4};

    public String validate(GameConfig config) {
        StringBuilder res = new StringBuilder();

        if (config.getGameRTP() <= 0) {
            return "Wrong gameRTP: " + config.getGameRTP();
        }

        config.getWeaponDropSpecialWeaponRTPTargetEn().forEach((integer, aDouble) -> {
            if (aDouble < EPS) {
                res.append("Wrong weaponDropSpecialWeaponRTPTargetEn: ").append(aDouble).append("\n");
            }
        });

        config.getWeaponDropSpecialWeaponRTPTargetBoss().forEach((integer, aDouble) -> {
            if (aDouble < EPS) {
                res.append("Wrong wWeaponDropSpecialWeaponRTPTargetBoss: ").append(aDouble).append("\n");
            }
        });

        Map<Integer, Integer> weaponPrices = config.getWeaponPrices();
        if (weaponPrices == null) {
            return "Missed weaponPrices";
        }

        Map<Integer, Map<Integer, Double>> weaponTargetsEn = config.getWeaponTargetsEn();
        if (weaponTargetsEn == null) {
            return "Missed weaponTargetsEn";
        }

        Map<Integer, Map<Integer, Double>> weaponTargetsBoss = config.getWeaponTargetsBoss();
        if (weaponTargetsBoss == null) {
            return "Missed weaponTargetsBoss";
        }

        Map<Integer, Map<Integer, Double>> criticalHitMultipliersTargetEn = config.getCriticalHitMultipliersTargetEn();
        if (criticalHitMultipliersTargetEn == null) {
            return "Missed criticalHitMultipliersTargetEn";
        }

        Arrays.stream(weapons).forEach(weaponId -> {
            if (weaponPrices.get(weaponId) == null || weaponPrices.get(weaponId) < 0) {
                res.append("Invalid price for weapon ").append(weaponId).append("\n");
            }
            if (weaponTargetsEn.get(weaponId) == null) {
                res.append("Missed targets list for weaponTargetsEn ").append(weaponId).append("\n");
            }
            if (weaponTargetsBoss.get(weaponId) == null) {
                res.append("Missed targets list for weaponTargetsBoss ").append(weaponId).append("\n");
            }
            if (criticalHitMultipliersTargetEn.get(weaponId) == null) {
                res.append("Missed criticalHitMultipliersTargetEn for weapon ").append(weaponId).append("\n");
            }
        });

        Map<Integer, Map<Integer, Double>> criticalHitMultipliersTargetBoss = config.getCriticalHitMultipliersTargetBoss();
        if (criticalHitMultipliersTargetBoss == null) {
            return "Missed criticalHitMultipliersTargetBoss";
        }

        Arrays.stream(weapons).forEach(weaponId -> {
            if (weaponPrices.get(weaponId) == null || weaponPrices.get(weaponId) < 0) {
                res.append("Invalid price for weapon ").append(weaponId).append("\n");
            }
            if (weaponTargetsEn.get(weaponId) == null) {
                res.append("Missed targets list for weapon ").append(weaponId).append("\n");
            }
            if (criticalHitMultipliersTargetBoss.get(weaponId) == null) {
                res.append("Missed criticalHitMultipliersTargetBoss for weapon ").append(weaponId).append("\n");
            }
        });

        if (config.getWeaponDropsTargetEn() == null) {
            return "Missed weaponDropsTargetEn";
        }

        for (EnemyType enemyType : EnemyRange.BASE_ENEMIES.getEnemies()) {
            EnemyData data = config.getEnemyData(enemyType, 0);
            if (data == null) {
                return "Missed data for enemy " + enemyType.name();
            }
            if (data.getPayout() < 0) {
                return "Missed payout for enemy " + enemyType.name();
            }
            if (data.getBaseTurretRTP() < EPS) {
                return "Missed baseTurretRTP for enemy " + enemyType.name();
            }
            if (data.getBaseTurretWeaponRTP() < EPS) {
                return "Missed baseTurretWeaponRTP for enemy " + enemyType.name();
            }
            if (data.getKillProbabilitiesTargetEn() == null) {
                return "Missed killProbabilitiesTargetEn for enemy " + enemyType.name();
            }

            if (data.getKillProbabilitiesTargetBoss() == null) {
                return "Missed killProbabilitiesTargetBoss for enemy " + enemyType.name();
            }
        }

        BossParams bossParams = config.getBossParams();

        if (bossParams == null) {
            return "Missed boss params";
        }

        if (bossParams.getBossEventProbability() < 0) {
            return "Missed boss event probability";
        }

        if (bossParams.getPickBossProbabilities() == null) {
            return "Missed pick boss probabilities";
        }

        if (bossParams.getDefeatMultiplier() == null) {
            return "Missed boss defeat multiplier";
        }

        if (bossParams.getBossPays() == null) {
            return "Missed boss pays";
        }

        Arrays.stream(BossType.values()).forEach(bossType -> {
            if (bossParams.getBossHP().get(bossType.getSkinId()) == null) {
                res.append("Missed boss HP for ").append(bossType.name());
            }
        });

        QuestParams questParams = config.getQuestParams();
        if (questParams == null) {
            return "Missed quest params";
        }

        if (questParams.getCollectToWin() < 0) {
            return "Invalid collectToWin amount";
        }

        if (questParams.getMinPrize() < 0) {
            return "Invalid minPrize amount";
        }

        if (questParams.getMaxPrize() < 0) {
            return "Invalid maxPrize amount";
        }

        if (questParams.getMinPrize() > questParams.getMaxPrize()) {
            return "Invalid minPrize, maxPrize amount: minPrize > maxPrize";
        }

        if (questParams.getDropProbabilityByWeaponTargetEn() == null || questParams.getDropProbabilityByWeaponTargetEn().isEmpty()) {
            return "Missed dropProbabilityByWeaponTargetEn by weapon";
        }

        if (questParams.getDropProbabilityByWeaponTargetBoss() == null || questParams.getDropProbabilityByWeaponTargetBoss().isEmpty()) {
            return "Missed dropProbabilityByWeaponTargetBoss by weapon";
        }

        Map<EnemyType, List<WeaponDrop>> weaponCarrierDropsTargetEn = config.getWeaponCarrierDropsTargetEn();
        if (weaponCarrierDropsTargetEn == null || weaponCarrierDropsTargetEn.isEmpty()) {
            return "Missed weaponCarrierDropsTargetEn drops";
        }

        EnemyRange.WEAPON_CARRIERS.getEnemies().forEach(weaponCarrier -> {
            if (!weaponCarrierDropsTargetEn.containsKey(weaponCarrier)) {
                res.append("Missed weaponCarrierDropsTargetEn for ").append(weaponCarrier);
            }
        });

        Map<EnemyType, List<WeaponDrop>> weaponCarrierDropsTargetBoss = config.getWeaponCarrierDropsTargetBoss();
        if (weaponCarrierDropsTargetBoss == null || weaponCarrierDropsTargetBoss.isEmpty()) {
            return "Missed weaponCarrierDropsTargetBoss drops";
        }

        EnemyRange.WEAPON_CARRIERS.getEnemies().forEach(weaponCarrier -> {
            if (!weaponCarrierDropsTargetBoss.containsKey(weaponCarrier)) {
                res.append("Missed weaponCarrierDropsTargetBoss for ").append(weaponCarrier);
            }
        });

        if (res.length() > 0) {
            return res.toString();
        }

        return "";
    }
}
