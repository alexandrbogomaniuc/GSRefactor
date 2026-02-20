package com.betsoft.casino.mp.dragonstone.model.math.config;

import java.util.Map;

public class BossParams {
    private double averageSmallPay;
    private double killedTurretRTP;
    private int killedPay;
    private Map<Integer, Double> smallPays;

    // New fields
    private int maxHP;
    private int damageDivider;
    private int minPay;
    private int maxPay;
    private int fixedPay;
    private double minRTPFixedPay;
    private double maxRTPFixedPay;
    private double minRTPSmallPay;
    private double maxRTPSmallPay;
    private double minRTPBigPay;
    private double maxRTPBigPay;
    private int pickWeight;
    private int lowPayThreshold;

    private final Map<Integer, Double> weaponProbabilityMultiplier;

    public BossParams(double averageSmallPay, double killedTurretRTP, int killedPay, Map<Integer, Double> smallPays, int maxHP,
                      int damageDivider, int minPay, int maxPay, int fixedPay, double minRTPFixedPay, double maxRTPFixedPay,
                      double minRTPSmallPay, double maxRTPSmallPay, double minRTPBigPay, double maxRTPBigPay, int pickWeight,
                      int lowPayThreshold, Map<Integer, Double> weaponProbabilityMultiplier) {
        this.averageSmallPay = averageSmallPay;
        this.killedTurretRTP = killedTurretRTP;
        this.killedPay = killedPay;
        this.smallPays = smallPays;
        this.maxHP = maxHP;
        this.damageDivider = damageDivider;
        this.minPay = minPay;
        this.maxPay = maxPay;
        this.fixedPay = fixedPay;
        this.minRTPFixedPay = minRTPFixedPay;
        this.maxRTPFixedPay = maxRTPFixedPay;
        this.minRTPSmallPay = minRTPSmallPay;
        this.maxRTPSmallPay = maxRTPSmallPay;
        this.minRTPBigPay = minRTPBigPay;
        this.maxRTPBigPay = maxRTPBigPay;
        this.pickWeight = pickWeight;
        this.lowPayThreshold = lowPayThreshold;
        this.weaponProbabilityMultiplier = weaponProbabilityMultiplier;
    }

    public double getAverageSmallPay() {
        return averageSmallPay;
    }

    public double getKilledTurretRTP() {
        return killedTurretRTP;
    }

    public int getKilledPay() {
        return killedPay;
    }

    public Map<Integer, Double> getSmallPays() {
        return smallPays;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public int getDamageDivider() {
        return damageDivider;
    }

    public int getMinPay() {
        return minPay;
    }

    public int getMaxPay() {
        return maxPay;
    }

    public int getFixedPay() {
        return fixedPay;
    }

    public double getMinRTPFixedPay() {
        return minRTPFixedPay;
    }

    public double getMaxRTPFixedPay() {
        return maxRTPFixedPay;
    }

    public double getMinRTPSmallPay() {
        return minRTPSmallPay;
    }

    public double getMaxRTPSmallPay() {
        return maxRTPSmallPay;
    }

    public double getMinRTPBigPay() {
        return minRTPBigPay;
    }

    public double getMaxRTPBigPay() {
        return maxRTPBigPay;
    }

    public int getPickWeight() {
        return pickWeight;
    }

    public int getLowPayThreshold() {
        return lowPayThreshold;
    }

    public Double getWeaponProbabilityMultiplier(Integer weaponId) {
        return weaponProbabilityMultiplier.get(weaponId);
    }
}
