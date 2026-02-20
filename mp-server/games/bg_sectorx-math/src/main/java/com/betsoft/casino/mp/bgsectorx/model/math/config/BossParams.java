package com.betsoft.casino.mp.bgsectorx.model.math.config;

import java.util.Map;

public class BossParams {
    private int minPay;
    private int maxPay;
    private int pickWeight;
    private int fixedPay;
    private Map<Integer, Double> payWeights;
    private Map<Integer, Double> Pkill;


    public BossParams(int minPay, int maxPay, int fixedPay, int pickWeight, Map<Integer, Double> payWeights, Map<Integer, Double> pkill) {
        this.minPay = minPay;
        this.maxPay = maxPay;
        this.fixedPay = fixedPay;
        this.pickWeight = pickWeight;
        this.payWeights = payWeights;
        Pkill = pkill;
    }

    public int getMinPay() {
        return minPay;
    }

    public int getMaxPay() {
        return maxPay;
    }

    public int getPickWeight() {
        return pickWeight;
    }

    public Map<Integer, Double> getPayWeights() {
        return payWeights;
    }

    public Map<Integer, Double> getPkill() {
        return Pkill;
    }

    public int getFixedPay() { return fixedPay; }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BossParams{");
        sb.append("minPay=").append(minPay);
        sb.append(", maxPay=").append(maxPay);
        sb.append(", pickWeight=").append(pickWeight);
        sb.append(", payWeights=").append(payWeights);
        sb.append(", Pkill=").append(Pkill);
        sb.append('}');
        return sb.toString();
    }
}
