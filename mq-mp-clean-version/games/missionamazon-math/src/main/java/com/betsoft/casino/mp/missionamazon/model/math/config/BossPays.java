package com.betsoft.casino.mp.missionamazon.model.math.config;

import java.util.Map;

public class BossPays {
    private Map<Integer, Map<Integer, Double>> partialPaysWeights;
    private Map<Integer, Map<Integer, Double>> partialPaysProbTargetEn;
    private Map<Integer, Map<Integer, Double>> partialPaysProbTargetBoss;

    public BossPays(Map<Integer, Map<Integer, Double>> partialPaysWeights,
                    Map<Integer, Map<Integer, Double>> partialPaysProbTargetEn,
                    Map<Integer, Map<Integer, Double>> partialPaysProbTargetBoss) {
        this.partialPaysWeights = partialPaysWeights;
        this.partialPaysProbTargetEn = partialPaysProbTargetEn;
        this.partialPaysProbTargetBoss = partialPaysProbTargetBoss;
    }

    public Map<Integer, Map<Integer, Double>> getPartialPaysWeights() {
        return partialPaysWeights;
    }

    public Map<Integer, Double> getPartialPaysWeights(int weaponId) {
        return partialPaysWeights.get(weaponId);
    }

    public Map<Integer, Map<Integer, Double>> getPartialPaysProbTargetEn() {
        return partialPaysProbTargetEn;
    }

    public Map<Integer, Map<Integer, Double>> getPartialPaysProbTargetBoss() {
        return partialPaysProbTargetBoss;
    }

    @Override
    public String toString() {
        return "BossPays{" +
                "partialPaysWeights=" + partialPaysWeights +
                ", partialPaysProbTargetEn=" + partialPaysProbTargetEn +
                ", partialPaysProbTargetBoss=" + partialPaysProbTargetBoss +
                '}';
    }
}
