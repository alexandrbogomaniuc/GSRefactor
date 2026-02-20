package com.betsoft.casino.mp.bgmissionamazon.model.math.config;

import com.betsoft.casino.mp.bgmissionamazon.model.math.EnemyRange;
import com.betsoft.casino.mp.bgmissionamazon.model.math.EnemyType;

import java.util.List;
import java.util.stream.Collectors;

public class SpawnConfigValidator {

    public String validate(SpawnConfig spawnConfig) {

        if (spawnConfig.getEnemiesWithPredefinedTrajectoriesMax() < 0) {
            return "Invalid totalEnemiesMax";
        }

        if (spawnConfig.getInitialSpawnEnemies() < 0) {
            return "Invalid initialSpawnEnemies";
        }

        if (spawnConfig.getSwarmEnemiesMax() < 0) {
            return "Invalid swarmEnemiesMax";
        }

        if (spawnConfig.getEnemiesWithPredefinedTrajectoriesMax() < 0) {
            return "Invalid totalEnemiesMax";
        }

        if (spawnConfig.getInitialSpawnEnemies() < 0) {
            return "Invalid initialSpawnEnemies";
        }

        if (spawnConfig.getSwarmEnemiesMax() < 0) {
            return "Invalid swarmEnemiesMax";
        }


        if (spawnConfig.getStaticEnemyMax() < 0) {
            return "Invalid staticEnemyMax";
        }

        if (spawnConfig.getStaticStayTimes() == null || spawnConfig.getStaticStayTimes().isEmpty()) {
            return "Invalid staticStayTimes";
        }

        for (long stayTime : spawnConfig.getStaticStayTimes()) {
            if (stayTime <= 0) {
                return "Invalid staticStayTimes element: " + stayTime;
            }
        }

        if (spawnConfig.getTeleportingStayTimes() == null || spawnConfig.getTeleportingStayTimes().isEmpty()) {
            return "Invalid teleportingStayTimes";
        }

        for (long stayTime : spawnConfig.getTeleportingStayTimes()) {
            if (stayTime <= 0) {
                return "Invalid teleportingStayTimes element: " + stayTime;
            }
        }

        if (spawnConfig.getTeleportingHopsNumber() == null || spawnConfig.getTeleportingHopsNumber().isEmpty()) {
            return "Invalid teleportingHopsNumber";
        }

        for (long hopValue : spawnConfig.getTeleportingHopsNumber()) {
            if (hopValue <= 0) {
                return "Invalid teleportingHopsNumber element: " + hopValue;
            }
        }

        if (spawnConfig.getKillEnemyByIds() == null || spawnConfig.getKillEnemyByIds().isEmpty()) {
            return "Invalid killEnemyByIds (use [-1] for no emulation)";
        }

        if (spawnConfig.getKillCertainNumberOfEnemies() == null || spawnConfig.getKillCertainNumberOfEnemies().isEmpty()) {
            return "Invalid killCertainNumberOfEnemies (use [-1] for no emulation)";
        }

        for (int killNumber : spawnConfig.getKillCertainNumberOfEnemies()) {
            if (killNumber <= 0 && killNumber != -1) {
                return "Invalid killCertainNumberOfEnemies element " + killNumber + " (use [-1] for no emulation)";
            }
        }

        String weaponCarrierTypesCheck = checkIdsForEnemyRangeBelong(EnemyRange.WEAPON_CARRIERS, spawnConfig.getWeaponCarrierTypes(),
                "weaponCarrierTypes");
        if (!weaponCarrierTypesCheck.isEmpty()) {
            return weaponCarrierTypesCheck;
        }

        if (spawnConfig.getWeaponCarrierSpawnTimeDelay() == null || spawnConfig.getWeaponCarrierSpawnTimeDelay().isEmpty()) {
            return "Invalid weaponCarrierSpawnTimeDelay";
        }

        for (long stayTime : spawnConfig.getWeaponCarrierSpawnTimeDelay()) {
            if (stayTime <= 0) {
                return "Invalid weaponCarrierSpawnTimeDelay element: " + stayTime;
            }
        }

        if (spawnConfig.getStaticEnemiesSpawnTimeDelay() == null || spawnConfig.getStaticEnemiesSpawnTimeDelay().isEmpty()) {
            return "Invalid staticEnemiesSpawnTimeDelay";
        }

        for (long stayTime : spawnConfig.getStaticEnemiesSpawnTimeDelay()) {
            if (stayTime <= 0) {
                return "Invalid staticEnemiesSpawnTimeDelay element: " + stayTime;
            }
        }

        if (spawnConfig.getWeaponCarrierStayTimes() == null || spawnConfig.getWeaponCarrierStayTimes().isEmpty()) {
            return "Invalid weaponCarrierStayTimes";
        }

        for (long stayTime : spawnConfig.getWeaponCarrierStayTimes()) {
            if (stayTime <= 0) {
                return "Invalid weaponCarrierStayTimes element: " + stayTime;
            }
        }

        if (spawnConfig.getWeaponCarrierOffsetTime() == null || spawnConfig.getWeaponCarrierOffsetTime().isEmpty()) {
            return "Invalid weaponCarrierOffsetTime";
        }

        for (long offset : spawnConfig.getWeaponCarrierOffsetTime()) {
            if (offset <= 0) {
                return "Invalid weaponCarrierOffsetTime element: " + offset;
            }
        }

        String highPayEnemiesTypesCheck = checkIdsForEnemyRangeBelong(EnemyRange.HIGH_PAY_ENEMIES, spawnConfig.getHighPayEnemies(),
                "highPayEnemies");
        if (!highPayEnemiesTypesCheck.isEmpty()) {
            return weaponCarrierTypesCheck;
        }

        String midPayEnemiesTypesCheck = checkIdsForEnemyRangeBelong(EnemyRange.MID_PAY_ENEMIES, spawnConfig.getMidPayEnemies(),
                "midPayEnemies");
        if (!midPayEnemiesTypesCheck.isEmpty()) {
            return weaponCarrierTypesCheck;
        }

        String lowPayEnemiesTypesCheck = checkIdsForEnemyRangeBelong(EnemyRange.LOW_PAY_ENEMIES, spawnConfig.getLowPayEnemies(),
                "lowPayEnemies");
        if (!lowPayEnemiesTypesCheck.isEmpty()) {
            return weaponCarrierTypesCheck;
        }

        if (spawnConfig.getAllEnemiesMax() < 0) {
            return "Invalid allEnemiesMax";
        }

        if (spawnConfig.getDividers() == null || spawnConfig.getDividers().isEmpty()) {
            return "Invalid dividers";
        }

        for (double divider : spawnConfig.getDividers()) {
            if (divider <= 0) {
                return "Invalid dividers";
            }
        }

        if (spawnConfig.getHighPayWeights() == null || spawnConfig.getHighPayWeights().isEmpty() || spawnConfig.getDividers().size() != spawnConfig.getHighPayWeights().size()) {
            return "Invalid highPayWeights";
        }

        for (double weight : spawnConfig.getHighPayWeights()) {
            if (weight < 0) {
                return "Invalid highPayWeights";
            }
        }

        if (spawnConfig.getMidPayWeights() == null || spawnConfig.getMidPayWeights().isEmpty() || spawnConfig.getDividers().size() != spawnConfig.getMidPayWeights().size()) {
            return "Invalid midPayWeights";
        }

        for (double weight : spawnConfig.getMidPayWeights()) {
            if (weight < 0) {
                return "Invalid midPayWeights";
            }
        }

        if (spawnConfig.getLowPayWeights() == null || spawnConfig.getLowPayWeights().isEmpty() || spawnConfig.getDividers().size() != spawnConfig.getLowPayWeights().size()) {
            return "Invalid lowPayWeights";
        }

        for (double weight : spawnConfig.getLowPayWeights()) {
            if (weight < 0) {
                return "Invalid lowPayWeights";
            }
        }

        if (spawnConfig.getTimeSlices() == null || spawnConfig.getTimeSlices().isEmpty()) {
            return "Invalid timeSlices";
        }

        if (spawnConfig.getTimeSlices().get(0) != 0) {
            return "Invalid timeSlices(must start with 0)";
        }

        int tempTime = -1;
        for (int time : spawnConfig.getTimeSlices()) {
            if (tempTime >= time) {
                return "Invalid timeSlices(must be in strictly increasing order)";
            }
            tempTime = time;
        }

        if (spawnConfig.getWeightsByTimeSlice() == null || spawnConfig.getWeightsByTimeSlice().size() != spawnConfig.getTimeSlices().size() - 1) {
            return "Invalid weightsByTimeSlice";
        }

        for (List<Integer> weights : spawnConfig.getWeightsByTimeSlice()) {
            if (weights == null || weights.size() != 3) {
                return "Invalid weightsByTimeSlice";
            }
            int sum = weights.stream()
                    .reduce(0, Integer::sum);
            if (sum != 100) {
                return "Invalid weightsByTimeSlice(weights for (HP + MP + LP) must be 100)";
            }
        }

        SpawnBossParams bossParams = spawnConfig.getBossParams();
        if (bossParams == null) {
            return "bossParams is null";
        }

        if (bossParams.isBoundariesCheck() && (bossParams.getA() >= 1 || bossParams.getA() <= 0)) {
            return "bossParams: A must be positive and (much) less than 1";
        }

        if (bossParams.isBoundariesCheck() && (bossParams.getMu() < bossParams.getT1() || bossParams.getMu() > bossParams.getT2())) {
            return "bossParams: mu must between [T1, T2]";
        }

        if (bossParams.isBoundariesCheck() && bossParams.getSigma() > 50) {
            return "bossParams: sigma should not more than 50";
        }

        if (bossParams.isBoundariesCheck() && (bossParams.getLambda() < 0 || bossParams.getLambda() > 0.5)) {
            return "bossParams: lambda must be positive and much less than 0.5;";
        }

        if (bossParams.isBoundariesCheck() && (bossParams.getT1() < 15 || bossParams.getT1() > 70)) {
            return "bossParams: Must be 15<T1<70";
        }

        if (bossParams.isBoundariesCheck() && (bossParams.getT2() < 60 || bossParams.getT2() > 70)) {
            return "bossParams: Must be 60<T2<70";
        }

        if (bossParams.isBoundariesCheck() && (bossParams.getT1() > bossParams.getT2())) {
            return "bossParams: Must be T2>T1";
        }

        return "";
    }

    private String checkIdsForEnemyRangeBelong(EnemyRange enemyRange, List<Integer> inputIds, String templateMessage) {
        List<Integer> enemyRangeIds = EnemyRange.getEnemiesFromRanges(enemyRange).stream()
                .map(EnemyType::getId)
                .collect(Collectors.toList());

        if (inputIds == null || inputIds.isEmpty()) {
            return "Invalid " + templateMessage;
        }

        for (int enemyType : inputIds) {
            if (!enemyRangeIds.contains(enemyType)) {
                return "Invalid" + templateMessage + " id: " + enemyType;
            }
        }
        return "";
    }
}