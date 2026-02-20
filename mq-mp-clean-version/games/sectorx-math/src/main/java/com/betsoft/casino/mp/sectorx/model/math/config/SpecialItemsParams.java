package com.betsoft.casino.mp.sectorx.model.math.config;

public class SpecialItemsParams {
    private final double a;
    private final int t0;
    private final int t1;
    private final int ta;
    private final double delta;
    private final int maxItems;


    public SpecialItemsParams(double a, int t0, int t1, int ta, double delta, int maxItems) {
        this.a = a;
        this.t0 = t0;
        this.t1 = t1;
        this.ta = ta;
        this.delta = delta;
        this.maxItems = maxItems;
    }

    public double getA() {
        return a;
    }

    public int getT0() {
        return t0;
    }

    public int getT1() {
        return t1;
    }

    public int getTa() {
        return ta;
    }

    public double getDelta() {
        return delta;
    }

    public int getMaxItems() {
        return maxItems;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpecialItemsParams{");
        sb.append("a=").append(a);
        sb.append(", t0=").append(t0);
        sb.append(", t1=").append(t1);
        sb.append(", ta=").append(ta);
        sb.append(", delta=").append(delta);
        sb.append(", maxItems=").append(maxItems);
        sb.append('}');
        return sb.toString();
    }
}
