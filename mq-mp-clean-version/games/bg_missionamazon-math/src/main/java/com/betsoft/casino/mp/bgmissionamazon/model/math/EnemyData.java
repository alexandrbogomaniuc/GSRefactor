package com.betsoft.casino.mp.bgmissionamazon.model.math;

import com.betsoft.casino.mp.model.SpecialWeaponType;

import java.util.Map;

public class EnemyData {
    private int payout;
    private double baseTurretKillProb;
    private Map<SpecialWeaponType, Double> killProbabilitiesTargetEn;
    private Map<SpecialWeaponType, Double> killProbabilitiesTargetBoss;
    private boolean enabled;
    private double baseTurretSWDrop;

    public EnemyData(int payout, double baseTurretKillProb, Map<SpecialWeaponType, Double> killProbabilitiesTargetEn,
                     Map<SpecialWeaponType, Double> killProbabilitiesTargetBoss, boolean enabled, double baseTurretSWDrop) {
        this.payout = payout;
        this.baseTurretKillProb = baseTurretKillProb;
        this.killProbabilitiesTargetEn = killProbabilitiesTargetEn;
        this.killProbabilitiesTargetBoss = killProbabilitiesTargetBoss;
        this.enabled = enabled;
        this.baseTurretSWDrop = baseTurretSWDrop;
    }

    public double getBaseTurretSWDrop() {
        return baseTurretSWDrop;
    }

    public double getBaseTurretKillProb() {
        return baseTurretKillProb;
    }

    public int getPayout() {
        return payout;
    }

    public Map<SpecialWeaponType, Double> getKillProbabilitiesTargetEn() {
        return killProbabilitiesTargetEn;
    }

    public Map<SpecialWeaponType, Double> getKillProbabilitiesTargetBoss() {
        return killProbabilitiesTargetBoss;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "EnemyData" + "[" +
                "payout=" + payout +
                ", killProbabilities=" + killProbabilitiesTargetEn +
                ", enabled=" + enabled +
                ']';
    }
}
