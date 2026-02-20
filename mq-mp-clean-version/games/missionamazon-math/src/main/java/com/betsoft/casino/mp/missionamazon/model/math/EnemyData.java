package com.betsoft.casino.mp.missionamazon.model.math;

import java.util.Map;

public class EnemyData {
    private int payout;
    private double baseTurretRTP;
    private double baseTurretWeaponRTP;
    private Map<Integer, Double> killProbabilitiesTargetEn;
    private Map<Integer, Double> killProbabilitiesTargetBoss;
    private boolean enabled;

    public EnemyData(int payout, double baseTurretRTP, double baseTurretWeaponRTP,
                     Map<Integer, Double> killProbabilitiesTargetEn, Map<Integer, Double> killProbabilitiesTargetBoss,
                     boolean enabled) {
        this.payout = payout;
        this.baseTurretRTP = baseTurretRTP;
        this.baseTurretWeaponRTP = baseTurretWeaponRTP;
        this.killProbabilitiesTargetEn = killProbabilitiesTargetEn;
        this.killProbabilitiesTargetBoss = killProbabilitiesTargetBoss;
        this.enabled = enabled;
    }

    public int getPayout() {
        return payout;
    }

    public double getBaseTurretRTP() {
        return baseTurretRTP;
    }

    public double getBaseTurretWeaponRTP() {
        return baseTurretWeaponRTP;
    }

    public Map<Integer, Double> getKillProbabilitiesTargetEn() {
        return killProbabilitiesTargetEn;
    }

    public Double getKillProbabilitiesTargetEn(int weaponId) {
        return killProbabilitiesTargetEn.get(weaponId);
    }

    public Map<Integer, Double> getKillProbabilitiesTargetBoss() {
        return killProbabilitiesTargetBoss;
    }

    public Double getKillProbabilitiesTargetBoss(int weaponId) {
        return killProbabilitiesTargetBoss.get(weaponId);
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "EnemyData" + "[" +
                "payout=" + payout +
                ", baseTurretRTP=" + baseTurretRTP +
                ", baseTurretWeaponRTP=" + baseTurretWeaponRTP +
                ", killProbabilities=" + killProbabilitiesTargetEn +
                ", enabled=" + enabled +
                ']';
    }
}
