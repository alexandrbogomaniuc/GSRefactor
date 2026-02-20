package com.betsoft.casino.mp.missionamazon.model.math.config;

public class BossShots {
    private double min;
    private double max;

    public BossShots(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    @Override
    public String toString() {
        return "BossShots{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }
}
