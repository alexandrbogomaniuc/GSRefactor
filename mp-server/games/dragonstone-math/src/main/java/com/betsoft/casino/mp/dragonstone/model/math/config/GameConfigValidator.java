package com.betsoft.casino.mp.dragonstone.model.math.config;

import com.betsoft.casino.mp.dragonstone.model.math.EnemyData;
import com.betsoft.casino.mp.dragonstone.model.math.EnemyType;

import static com.betsoft.casino.mp.dragonstone.model.math.EnemyRange.BASE_ENEMIES;

public class GameConfigValidator {

    private static final double EPS = 0.0000001;
    private int[] weapons = {-1, 9, 10, 4, 6, 7};

    public String validate(GameConfig config) {
        if (config.getGameRTP() <= 0) {
            return "Wrong gameRTP: " + config.getGameRTP();
        }

        StringBuilder res = new StringBuilder();
        config.getSlotContribution().forEach((integer, aDouble) -> {
            if (aDouble < EPS) {
                res.append("Wrong slotContribution: ").append(aDouble).append("\n");
            }
        });
        if (res.length() > 0) {
            return res.toString();
        }

        config.getWeaponDropSpecialWeaponRTP().forEach((integer, aDouble) -> {
            if (aDouble < EPS) {
                res.append("Wrong weaponDropSpecialWeaponRTP: ").append(aDouble).append("\n");
            }
        });
        if (res.length() > 0) {
            return res.toString();
        }

        if (config.getWeaponPrices() == null) {
            return "Missed weaponPrices";
        }
        if (config.getWeaponTargets() == null) {
            return "Missed weaponTargets";
        }
        for (int weaponId : weapons) {
            Integer weaponPrice = config.getWeaponPrices().get(weaponId);
            if (weaponPrice == null) {
                return "Missed price for weapon " + weaponId;
            }
            if (weaponPrice <= 0) {
                return "Invalid price for weapon " + weaponId;
            }
            if (config.getWeaponTargets().get(weaponId) == null) {
                return "Missed targets list for weapon " + weaponId;
            }
        }
        if (config.getWeaponDrops() == null) {
            return "Missed weaponDrops";
        }

        if (config.getCriticalHitMultipliers() == null) {
            return "Missed criticalHitMultipliers";
        }
        for (int weaponId : weapons) {
            if (config.getCriticalHitMultipliers().get(weaponId) == null) {
                return "Missed criticalHitMultipliers for weapon " + weaponId;
            }
        }
        for (EnemyType enemyType : BASE_ENEMIES.getEnemies()) {
            EnemyData data = config.getEnemyData(enemyType, 0);
            if (data == null) {
                return "Missed data for enemy " + enemyType.name();
            }
            if (data.getPayout() == 0 || data.isConfiguredWithMinMaxPay()) {
                return "Missed payout for enemy " + enemyType.name();
            }
            if (data.getBaseTurretRTP() < EPS) {
                return "Missed baseTurretRTP for enemy " + enemyType.name();
            }
            if (data.getBaseTurretWeaponRTP() < EPS) {
                return "Missed baseTurretWeaponRTP for enemy " + enemyType.name();
            }
            if (data.getKillProbabilities() == null) {
                return "Missed kill probabilities for enemy " + enemyType.name();
            }
        }

        if (config.getWeaponTargets() == null) {
            return "Missed weaponTargets";
        }
        if (config.getBoss() == null) {
            return "Missed boss params";
        }
        if (config.getBoss().getAverageSmallPay() < EPS) {
            return "Missed averageSmallPay for boss";
        }
        if (config.getBoss().getKilledTurretRTP() < EPS) {
            return "Missed killedTurretRTP for boss";
        }
        if (config.getBoss().getKilledPay() == 0) {
            return "Missed killedPay for boss";
        }
        if (config.getBoss().getSmallPays() == null) {
            return "Missed smallPays for boss";
        }
        if (config.getSlot() == null) {
            return "Missed slot config";
        }
        if (config.getSlot().getPays() == null) {
            return "Missed slot pays";
        }
        if (config.getSlot().getSpins() == 0) {
            return "Missed slot spins";
        }

        config.getSlot().getProbabilityByWeapon().forEach((integer, aDouble) -> {
            if (aDouble < EPS) {
                res.append("Wrong slot probabilies: ").append(aDouble).append("\n");
            }
        });

        if (res.length() > 0) {
            return res.toString();
        }


        if (config.getFragments() == null) {
            return "Missed Dragonstone fragments config";
        }
        if (config.getFragments().getCollectToSpawn() == 0) {
            return "Missed Dragonstone fragments collectToSpawn";
        }
        if (config.getFragments().getHitFrequency() == null) {
            return "Missed Dragonstone fragments hitFrequency";
        }
        for (int weaponId : weapons) {
            if (config.getFragments().getHitFrequency().get(weaponId) == null) {
                return "Missed Dragonstone fragments hitFrequency for weapon " + weaponId;
            }
        }
        return "";
    }
}
