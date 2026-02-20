package com.betsoft.casino.mp.sectorx.model.math;

public class EnemyData {
    private String name;
    private int pay;
    private int minPay;
    private int maxPay;
    private boolean enabled;

    public EnemyData(String name, int pay, int minPay, int maxPay, boolean enabled) {
        this.name = name;
        this.pay = pay;
        this.minPay = minPay;
        this.maxPay = maxPay;
        this.enabled = enabled;
    }

    public int getMinPay() {
        return minPay;
    }

    public void setMinPay(int minPay) {
        this.minPay = minPay;
    }

    public int getMaxPay() {
        return maxPay;
    }

    public void setMaxPay(int maxPay) {
        this.maxPay = maxPay;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPay() {
        return pay;
    }

    public void setPay(int pay) {
        this.pay = pay;
    }

    @Override
    public String toString() {
        return "EnemyData{" +
                "name='" + name + '\'' +
                ", pay=" + pay +
                ", minPay=" + minPay +
                ", maxPay=" + maxPay +
                ", enabled=" + enabled +
                '}';
    }
}
