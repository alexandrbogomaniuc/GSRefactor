package com.betsoft.casino.mp.bgmissionamazon.model.math.config;

import com.betsoft.casino.mp.bgmissionamazon.model.math.BossType;
import com.betsoft.casino.mp.bgmissionamazon.model.math.EnemyData;
import com.betsoft.casino.mp.bgmissionamazon.model.math.EnemyRange;
import com.betsoft.casino.mp.bgmissionamazon.model.math.EnemyType;
import com.betsoft.casino.mp.model.SpecialWeaponType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GameConfigValidator {
    private static final double EPS = 0.0000001;
    private static final int[] weapons = {-1, 7, 9, 10, 3, 4};

    public String validate(GameConfig config) {
        StringBuilder res = new StringBuilder();


        //deprecated validation
/*        Arrays.stream(weapons).forEach(weaponId -> {
            if (weaponPrices.get(weaponId) == null || weaponPrices.get(weaponId) < 0) {
                res.append("Invalid price for weapon ").append(weaponId).append("\n");
            }
            if (weaponTargets.get(weaponId) == null) {
                res.append("Missed targets list for weapon ").append(weaponId).append("\n");
            }
            if (criticalHitMultipliers.get(weaponId) == null) {
                res.append("Missed criticalHitMultipliers for weapon ").append(weaponId).append("\n");
            }
        });*/

        if (config.getWeaponDropsATargetEn() == null) {
            return "Missed weaponDropsATargetEn";
        }

        if (config.getWeaponDropsATargetBoss() == null) {
            return "Missed wWeaponDropsATargetBoss";
        }

        if (config.getWeaponDropsBTargetEn() == null) {
            return "Missed weaponDropsBTargetEn";
        }

        if (config.getWeaponDropsBTargetBoss() == null) {
            return "Missed weaponDropsBTargetBoss";
        }

        if (config.getTurretTargetsEn() == null) {
            return "Missed turretTargetsEn";
        }

        Map<SpecialWeaponType, Map<Integer, Double>> weaponTargetsEn = config.getWeaponTargetsEn();
        if (weaponTargetsEn == null) {
            return "Missed weaponTargetsEn";
        }

        if (config.getTurretTargetsBoss() == null) {
            return "Missed turretTargetsBoss";
        }

        Map<SpecialWeaponType, Map<Integer, Double>> weaponTargetsBoss = config.getWeaponTargetsBoss();
        if (weaponTargetsBoss == null) {
            return "Missed weaponTargetsBoss";
        }

        Map<Integer, Double> criticalHitTurretMultipliersTargetEn = config.getCriticalHitTurretMultipliersTargetEn();
        if (criticalHitTurretMultipliersTargetEn == null) {
            return "Missed criticalHitTurretMultipliersTargetEn";
        }

        Map<SpecialWeaponType, Map<Integer, Double>> criticalHitMultipliers = config.getCriticalHitMultipliersTargetEn();
        if (criticalHitMultipliers == null) {
            return "Missed criticalHitMultipliersTargetEn";
        }

        Map<Integer, Double> criticalHitTurretMultipliersTargetBoss = config.getCriticalHitTurretMultipliersTargetBoss();
        if (criticalHitTurretMultipliersTargetBoss == null) {
            return "Missed criticalHitTurretMultipliersTargetBoss";
        }

        Map<SpecialWeaponType, Map<Integer, Double>> criticalHitMultipliersTargetBoss = config.getCriticalHitMultipliersTargetBoss();
        if (criticalHitMultipliersTargetBoss == null) {
            return "Missed criticalHitMultipliersTargetBoss";
        }

        for (EnemyType enemyType : EnemyRange.BASE_ENEMIES.getEnemies()) {
            EnemyData data = config.getEnemyData(enemyType, 0);
            if (data == null) {
                return "Missed data for enemy " + enemyType.name();
            }
            if (data.getPayout() < 0) {
                return "Missed payout for enemy " + enemyType.name();
            }
            if (data.getKillProbabilitiesTargetEn() == null) {
                return "Missed killProbabilitiesTargetEn for enemy " + enemyType.name();
            }
            if (data.getKillProbabilitiesTargetBoss() == null) {
                return "Missed killProbabilitiesTargetBoss for enemy " + enemyType.name();
            }
            if (data.getBaseTurretSWDrop() < 0) {
                return "Missed baseTurretSWDrop for enemy " + enemyType.name();
            }
        }

        BossParams bossParams = config.getBossParams();

        if (bossParams == null) {
            return "Missed boss params";
        }

        if (bossParams.getPickBossProbabilities() == null) {
            return "Missed pick boss probabilities";
        }

        if (bossParams.getDefeatMultiplier() == null) {
            return "Missed boss defeat multiplier";
        }

        if (bossParams.getBossHP() == null) {
            return "Missed boss HP multiplier";
        }

        if (bossParams.getBossPays() == null) {
            return "Missed boss pays";
        }

        if (bossParams.getBossPays().getPartialPaysWeightsTurret() == null) {
            return "Missed partialPaysWeightsTurret";
        }

        if (bossParams.getBossPays().getPartialPaysWeights() == null) {
            return "Missed partialPaysWeights";
        }

        if (bossParams.getBossPays().getPartialPaysProbTurretTargetBoss() == null) {
            return "Missed partialPaysProbTurretTargetBoss";
        }

        if (bossParams.getBossPays().getPartialPaysProbTargetEn() == null) {
            return "Missed partialPaysProbTargetEn";
        }

        if (bossParams.getBossPays().getPartialPaysProbTargetBoss() == null) {
            return "Missed partialPaysProbTargetBoss";
        }

        Arrays.stream(BossType.values()).forEach(bossType -> {
            if (bossParams.getBossHP().get(bossType.getSkinId()) == null) {
                res.append("Missed boss pays for ").append(bossType.name());
            }
        });

        QuestParams questParams = config.getQuestParams();

        if (questParams == null) {
            return "Missed quest params";
        }

        if (questParams.getCollectToWin() < 0) {
            return "Invalid collectToWin amount";
        }

        if (questParams.getGemDrops() == null || questParams.getGemDrops().isEmpty()) {
            return "Missed gem drops";
        }

        if (questParams.getDropProbabilityByWeaponTargetEn() == null || questParams.getDropProbabilityByWeaponTargetEn().isEmpty()) {
            return "Missed drop dropProbabilityByWeaponTargetEn";
        }

        if (questParams.getDropProbabilityByWeaponTargetBoss() == null || questParams.getDropProbabilityByWeaponTargetBoss().isEmpty()) {
            return "Missed drop dropProbabilityByWeaponTargetBoss";
        }

        Map<EnemyType, List<WeaponDrop>> weaponCarrierDrops = config.getWeaponCarrierDropsTargetEn();
        if (weaponCarrierDrops == null || weaponCarrierDrops.isEmpty()) {
            return "Missed getWeaponCarrierDropsTargetEn";
        }

        EnemyRange.WEAPON_CARRIERS.getEnemies().forEach(weaponCarrier -> {
            if (!weaponCarrierDrops.containsKey(weaponCarrier)) {
                res.append("Missed getWeaponCarrierDropsTargetEn for ").append(weaponCarrier);
            }
        });

        Map<EnemyType, List<WeaponDrop>> weaponCarrierDropsTargetBoss = config.getWeaponCarrierDropsTargetBoss();
        if (weaponCarrierDropsTargetBoss == null || weaponCarrierDropsTargetBoss.isEmpty()) {
            return "Missed weaponCarrierDropsTargetBoss";
        }

        EnemyRange.WEAPON_CARRIERS.getEnemies().forEach(weaponCarrier -> {
            if (!weaponCarrierDropsTargetBoss.containsKey(weaponCarrier)) {
                res.append("Missed weaponCarrierDropsTargetBoss for ").append(weaponCarrier);
            }
        });

        if (config.getPSWDropsReTriggerOneTargetEn() == null) {
            return "Missed PSWDropsReTriggerOneTargetEn";
        }

        if (config.getPSWDropsReTriggerTwoTargetEn() == null) {
            return "Missed PSWDropsReTriggerTwoTargetEn";
        }

        if (config.getPSWDropsReTriggerOneTargetBoss() == null) {
            return "Missed PSWDropsReTriggerOneTargetBoss";
        }

        if (config.getPSWDropsReTriggerTwoTargetBoss() == null) {
            return "Missed PSWDropsReTriggerTwoTargetBoss";
        }

        if (config.getPowerUpMultipliers() == null) {
            return "Missed powerUpMultipliers";
        }

        if (res.length() > 0) {
            return res.toString();
        }

        return "";
    }
}
