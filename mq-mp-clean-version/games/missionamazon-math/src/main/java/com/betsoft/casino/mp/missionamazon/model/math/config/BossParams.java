package com.betsoft.casino.mp.missionamazon.model.math.config;

import com.betsoft.casino.mp.missionamazon.model.math.BossType;

import java.util.Map;

public class BossParams {
    private double bossEventProbability;
    private Map<Integer, Integer> bossHP;
    private Map<Integer, Double> pickBossProbabilities;
    private Map<Integer, Double> defeatMultiplier;
    private BossPays bossPays;
    private Map<Integer, BossShots> percentNumShotsSWOnBossTargetBoss;
    private Map<Integer, BossShots> percentNumShotsSWOnBossTargetEn;

    public BossParams(double bossEventProbability, Map<Integer, Integer> bossHP,
                      Map<Integer, Double> pickBossProbabilities, Map<Integer, Double> defeatMultiplier,
                      BossPays bossPays, Map<Integer, BossShots> percentNumShotsSWOnBossTargetBoss,
                      Map<Integer, BossShots> percentNumShotsSWOnBossTargetEn) {
        this.bossEventProbability = bossEventProbability;
        this.bossHP = bossHP;
        this.pickBossProbabilities = pickBossProbabilities;
        this.defeatMultiplier = defeatMultiplier;
        this.bossPays = bossPays;
        this.percentNumShotsSWOnBossTargetBoss = percentNumShotsSWOnBossTargetBoss;
        this.percentNumShotsSWOnBossTargetEn = percentNumShotsSWOnBossTargetEn;
    }

    public double getBossEventProbability() {
        return bossEventProbability;
    }

    public Map<Integer, Integer> getBossHP() {
        return bossHP;
    }

    public Map<Integer, Double> getPickBossProbabilities() {
        return pickBossProbabilities;
    }

    public Map<Integer, Double> getDefeatMultiplier() {
        return defeatMultiplier;
    }

    public BossPays getBossPays() {
        return bossPays;
    }

    public BossPays getBossPaysByType(BossType bossType) {
        return bossPays;
    }

    public Map<Integer, BossShots> getPercentNumShotsSWOnBossTargetBoss() {
        return percentNumShotsSWOnBossTargetBoss;
    }

    public Map<Integer, BossShots> getPercentNumShotsSWOnBossTargetEn() {
        return percentNumShotsSWOnBossTargetEn;
    }

    @Override
    public String toString() {
        return "BossParams{" +
                "bossEventProbability=" + bossEventProbability +
                ", bossHP=" + bossHP +
                ", pickBossProbabilities=" + pickBossProbabilities +
                ", defeatMultiplier=" + defeatMultiplier +
                ", bossPays=" + bossPays +
                ", precentnumShotsSWOnBossTargetBoss=" + percentNumShotsSWOnBossTargetBoss +
                ", precentnumShotsSWOnBossTargetEn=" + percentNumShotsSWOnBossTargetEn +
                '}';
    }
}
