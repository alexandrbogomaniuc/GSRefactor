package com.betsoft.casino.mp.dragonstone.model.math;

import java.util.Map;

public class EnemyData {
    private final int payout;
    private final int minPay;
    private final int maxPay;
    private final double baseTurretRTP;
    private final double baseTurretWeaponRTP;
    private final Map<Integer, Double> killProbabilities;
    private final Map<Integer, Double> weaponProbabilityMultiplier;

    private final double minRtp;
    private final double maxRtp;

    private final boolean enabled;

    public EnemyData(int payout, int minPay, int maxPay, double baseTurretRTP, double baseTurretWeaponRTP, Map<Integer, Double> killProbabilities, Map<Integer, Double> weaponProbabilityMultiplier, double minRtp, double maxRtp, boolean enabled) {
        this.payout = payout;
        this.minPay = minPay;
        this.maxPay = maxPay;
        this.baseTurretRTP = baseTurretRTP;
        this.baseTurretWeaponRTP = baseTurretWeaponRTP;
        this.killProbabilities = killProbabilities;
        this.weaponProbabilityMultiplier = weaponProbabilityMultiplier;
        this.minRtp = minRtp;
        this.maxRtp = maxRtp;
        this.enabled = enabled;
    }

    public int getPayout() {
        return payout;
    }

    public int getMaxPay() {
        return maxPay;
    }

    public int getMinPay() {
        return minPay;
    }

    public boolean isConfiguredWithMinMaxPay() {
        return minPay > 0 && maxPay > 0;
    }

    public double getBaseTurretRTP() {
        return baseTurretRTP;
    }

    public double getBaseTurretWeaponRTP() {
        return baseTurretWeaponRTP;
    }

    public Map<Integer, Double> getKillProbabilities() {
        return killProbabilities;
    }

    public Double getKillProbability(int weaponId) {
        return killProbabilities.get(weaponId);
    }

    public Double getWeaponProbabilityMultiplier(int weaponId) {
        return weaponProbabilityMultiplier.get(weaponId);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public double getMaxRtp() {
        return maxRtp;
    }

    public double getMinRtp() {
        return minRtp;
    }

    @Override
    public String toString() {
        return "EnemyData{" +
                "payout=" + payout +
                ", minPay=" + minPay +
                ", maxPay=" + maxPay +
                ", baseTurretRTP=" + baseTurretRTP +
                ", baseTurretWeaponRTP=" + baseTurretWeaponRTP +
                ", killProbabilities=" + killProbabilities +
                ", weaponProbabilityMultiplier=" + weaponProbabilityMultiplier +
                ", minRtp=" + minRtp +
                ", maxRtp=" + maxRtp +
                ", enabled=" + enabled +
                '}';
    }
}
