package com.betsoft.casino.mp.bgmissionamazon.model.math.config;

import java.util.List;
import java.util.Map;

public class QuestParams {
    private int collectToWin;
    private List<GemDrop> gemDrops;
    private Map<Integer, Double> dropProbabilityByWeaponTargetEn;
    private Map<Integer, Double> dropProbabilityByWeaponTargetBoss;

    public QuestParams(int collectToWin, List<GemDrop> gemDrops, Map<Integer, Double> dropProbabilityByWeaponTargetEn,
                       Map<Integer, Double> dropProbabilityByWeaponTargetBoss) {
        this.collectToWin = collectToWin;
        this.gemDrops = gemDrops;
        this.dropProbabilityByWeaponTargetEn = dropProbabilityByWeaponTargetEn;
        this.dropProbabilityByWeaponTargetBoss = dropProbabilityByWeaponTargetBoss;
    }

    public int getCollectToWin() {
        return collectToWin;
    }

    public List<GemDrop> getGemDrops() {
        return gemDrops;
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
                ", gemDrops=" + gemDrops +
                ", dropProbabilityByWeaponTargetEn=" + dropProbabilityByWeaponTargetEn +
                ", dropProbabilityByWeaponTargetBoss=" + dropProbabilityByWeaponTargetBoss +
                '}';
    }
}
