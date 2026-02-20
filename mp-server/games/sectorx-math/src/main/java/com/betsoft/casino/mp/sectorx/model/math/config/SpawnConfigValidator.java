package com.betsoft.casino.mp.sectorx.model.math.config;


import com.betsoft.casino.mp.sectorx.model.math.EnemyRange;
import com.betsoft.casino.mp.sectorx.model.math.EnemyType;

import java.util.List;
import java.util.stream.Collectors;

public class SpawnConfigValidator {

    public String validate(SpawnConfig spawnConfig) {

        for (double weight : spawnConfig.getHighPayWeights()) {
            if (weight < 0) {
                return "Invalid highPayWeights";
            }
        }

        for (double weight : spawnConfig.getMidPayWeights()) {
            if (weight < 0) {
                return "Invalid midPayWeights";
            }
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

        NewBossParams bossParams = spawnConfig.getBossParams();
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

        if (bossParams.isBoundariesCheck() && (bossParams.getT1() < 100 || bossParams.getT1() > 300)) {
            return "bossParams: Must be 100<T1<300";
        }

        if (bossParams.isBoundariesCheck() && (bossParams.getT2() < 200 || bossParams.getT2() > 280)) {
            return "bossParams: Must be 200<T2<280";
        }

        if (bossParams.isBoundariesCheck() && (bossParams.getT1() > bossParams.getT2())) {
            return "bossParams: Must be T2>T1";
        }

        HugePayItemsParams hugePayItemsParams = spawnConfig.getHugePayEnemiesParams();
        if (hugePayItemsParams == null) {
            return "hugePayItemsParams is null";
        }

        checkParams(hugePayItemsParams.getA(), hugePayItemsParams.getT1(), hugePayItemsParams.getDelta(),
                "hugePayItemsParams");

        SpecialItemsParams specialItemsParams = spawnConfig.getSpecialItemsParams();
        if (specialItemsParams == null) {
            return "specialItemsParams is null";
        }

        checkParams(specialItemsParams.getA(), specialItemsParams.getT1(), specialItemsParams.getDelta(),
                "specialItemsParams");

        return "";
    }

    private String checkParams(double A, double T1, double delta, String className) {
        if (A >= 1 || A <= 0) {
            return className + ": A must be positive and (much) less than 1";
        }

        if (T1 < 100 || T1 > 300) {
            return className + ": hugePayItemsParams: Must be 100<T1<300";
        }

        if (delta < 0 || delta > 1) {
            return className + ": hugePayItemsParams: delta must be positive value from 0 to 1";
        }
        return "";
    }

    private String checkIdsForEnemyRangeBelong (EnemyRange enemyRange, List<Integer> inputIds, String templateMessage) {
        List<Integer> enemyRangeIds = EnemyRange.getEnemiesFromRanges(enemyRange).stream()
                .map(EnemyType::getId)
                .collect(Collectors.toList());

        if (inputIds == null || inputIds.isEmpty()) {
            return "Invalid " + templateMessage;
        }

        for(int enemyType : inputIds) {
            if (!enemyRangeIds.contains(enemyType)) {
                return "Invalid" + templateMessage + " id: " + enemyType;
            }
        }
        return "";
    }
}