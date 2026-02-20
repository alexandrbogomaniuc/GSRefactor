package com.betsoft.casino.mp.sectorx.model.math.config;

import java.util.Map;
import java.util.Objects;

public class BossParams {
    private int maxHP;

    private double damageDivider;
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

    public BossParams(int maxHP, double damageDivider, int minPay, int maxPay, int fixedPay, double minRTPFixedPay, double maxRTPFixedPay, double minRTPSmallPay, double maxRTPSmallPay, double minRTPBigPay, double maxRTPBigPay, int pickWeight, int lowPayThreshold) {
        this.maxHP = maxHP;
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
        this.damageDivider = damageDivider;
    }

    public int getMaxHP() {
        return maxHP;
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

    public double getDamageDivider() {
        return damageDivider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BossParams that = (BossParams) o;
        return maxHP == that.maxHP && Double.compare(damageDivider, that.damageDivider) == 0
                && minPay == that.minPay && maxPay == that.maxPay && fixedPay == that.fixedPay
                && Double.compare(minRTPFixedPay, that.minRTPFixedPay) == 0
                && Double.compare(maxRTPFixedPay, that.maxRTPFixedPay) == 0
                && Double.compare(minRTPSmallPay, that.minRTPSmallPay) == 0
                && Double.compare(maxRTPSmallPay, that.maxRTPSmallPay) == 0
                && Double.compare(minRTPBigPay, that.minRTPBigPay) == 0
                && Double.compare(maxRTPBigPay, that.maxRTPBigPay) == 0
                && pickWeight == that.pickWeight
                && lowPayThreshold == that.lowPayThreshold;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxHP, damageDivider, minPay, maxPay, fixedPay, minRTPFixedPay,
                maxRTPFixedPay, minRTPSmallPay, maxRTPSmallPay, minRTPBigPay, maxRTPBigPay,
                pickWeight, lowPayThreshold);
    }

    @Override
    public String toString() {
        return "BossParams{" +
                "maxHP=" + maxHP +
                ", damageDivider=" + damageDivider +
                ", minPay=" + minPay +
                ", maxPay=" + maxPay +
                ", fixedPay=" + fixedPay +
                ", minRTPFixedPay=" + minRTPFixedPay +
                ", maxRTPFixedPay=" + maxRTPFixedPay +
                ", minRTPSmallPay=" + minRTPSmallPay +
                ", maxRTPSmallPay=" + maxRTPSmallPay +
                ", minRTPBigPay=" + minRTPBigPay +
                ", maxRTPBigPay=" + maxRTPBigPay +
                ", pickWeight=" + pickWeight +
                ", lowPayThreshold=" + lowPayThreshold +
                '}';
    }
}
