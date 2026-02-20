package com.betsoft.casino.mp.missionamazon.model.math.config;

import java.util.Map;

public class QuestParams {
    private int collectToWin;
    private int minPrize;
    private int maxPrize;
    private Map<Integer, Double> dropProbabilityByWeaponTargetEn;
    private Map<Integer, Double> dropProbabilityByWeaponTargetBoss;

    public QuestParams(int collectToWin, int minPrize, int maxPrize,
                       Map<Integer, Double> dropProbabilityByWeaponTargetEn,
                       Map<Integer, Double> dropProbabilityByWeaponTargetBoss) {
        this.collectToWin = collectToWin;
        this.minPrize = minPrize;
        this.maxPrize = maxPrize;
        this.dropProbabilityByWeaponTargetEn = dropProbabilityByWeaponTargetEn;
        this.dropProbabilityByWeaponTargetBoss = dropProbabilityByWeaponTargetBoss;
    }

    public int getCollectToWin() {
        return collectToWin;
    }

    public int getMinPrize() {
        return minPrize;
    }

    public int getMaxPrize() {
        return maxPrize;
    }

    public Map<Integer, Double> getDropProbabilityByWeaponTargetEn() {
        return dropProbabilityByWeaponTargetEn;
    }

    public Map<Integer, Double> getDropProbabilityByWeaponTargetBoss() {
        return dropProbabilityByWeaponTargetBoss;
    }


    @Override
    public String toString() {
        return "QuestParams{" +
                "collectToWin=" + collectToWin +
                ", minPrize=" + minPrize +
                ", maxPrize=" + maxPrize +
                ", dropProbabilityByWeaponTargetEn=" + dropProbabilityByWeaponTargetEn +
                ", dropProbabilityByWeaponTargetBoss=" + dropProbabilityByWeaponTargetBoss +
                '}';
    }
}
