package com.betsoft.casino.mp.bgmissionamazon.model.math.config;

import com.betsoft.casino.mp.model.SpecialWeaponType;

import java.util.Map;

public class BossParams {
    private Map<Integer, Double> pickBossProbabilities;
    private Map<Integer, Double> defeatMultiplier;
    private BossPays bossPays;
    private Map<Integer, Integer> bossHP;
    private Map<SpecialWeaponType, BossShots> percentNumShotsSWOnBossTargetBoss;
    private Map<SpecialWeaponType, BossShots> percentNumShotsSWOnBossTargetEn;


    public BossParams(Map<Integer, Double> pickBossProbabilities,
                      Map<Integer, Double> defeatMultiplier, BossPays bossPays,
                      Map<Integer, Integer> bossHP, Map<SpecialWeaponType, BossShots> percentNumShotsSWOnBossTargetBoss,
                      Map<SpecialWeaponType, BossShots> percentNumShotsSWOnBossTargetEn) {
        this.pickBossProbabilities = pickBossProbabilities;
        this.defeatMultiplier = defeatMultiplier;
        this.bossPays = bossPays;
        this.bossHP = bossHP;
        this.percentNumShotsSWOnBossTargetBoss = percentNumShotsSWOnBossTargetBoss;
        this.percentNumShotsSWOnBossTargetEn = percentNumShotsSWOnBossTargetEn;
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

    public Map<Integer, Integer> getBossHP() {
        return bossHP;
    }

    public Map<SpecialWeaponType, BossShots> getPercentNumShotsSWOnBossTargetBoss() {
        return percentNumShotsSWOnBossTargetBoss;
    }

    public Map<SpecialWeaponType, BossShots> getPercentNumShotsSWOnBossTargetEn() {
        return percentNumShotsSWOnBossTargetEn;
    }

    @Override
    public String toString() {
        return "BossParams{" +
                "pickBossProbabilities=" + pickBossProbabilities +
                ", defeatMultiplier=" + defeatMultiplier +
                ", bossPays=" + bossPays +
                ", bossHP=" + bossHP +
                ", precentnumShotsSWOnBossTargetBoss=" + percentNumShotsSWOnBossTargetBoss +
                ", precentnumShotsSWOnBossTargetEn=" + percentNumShotsSWOnBossTargetEn +
                '}';
    }
}
