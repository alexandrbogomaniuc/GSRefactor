package com.betsoft.casino.mp.missionamazon.model.math.config;

public class NewBossParams {
    private double a;
    private double mu;
    private double sigma;
    private double lambda;
    private int t1;
    private int t2;
    private int ta;
    private int maxBosses;
    private boolean boundariesCheck;

    public NewBossParams(double a, double mu, double sigma, double lambda, int t1, int t2, int ta, int maxBosses, boolean boundariesCheck) {
        this.a = a;
        this.mu = mu;
        this.sigma = sigma;
        this.lambda = lambda;
        this.t1 = t1;
        this.t2 = t2;
        this.ta = ta;
        this.maxBosses = maxBosses;
        this.boundariesCheck = boundariesCheck;
    }

    public double getA() {
        return a;
    }

    public double getMu() {
        return mu;
    }

    public double getSigma() {
        return sigma;
    }

    public double getLambda() {
        return lambda;
    }

    public int getT1() {
        return t1;
    }

    public int getT2() {
        return t2;
    }

    public int getTa() {
        return ta;
    }

    public int getMaxBosses() {
        return maxBosses;
    }

    public boolean isBoundariesCheck() {
        return boundariesCheck;
    }

    @Override
    public String toString() {
        return "NewBossParams{" +
                "a=" + a +
                ", mu=" + mu +
                ", sigma=" + sigma +
                ", lambda=" + lambda +
                ", t1=" + t1 +
                ", t2=" + t2 +
                ", ta=" + ta +
                ", maxBosses=" + maxBosses +
                ", boundariesCheck=" + boundariesCheck +
                '}';
    }
}
