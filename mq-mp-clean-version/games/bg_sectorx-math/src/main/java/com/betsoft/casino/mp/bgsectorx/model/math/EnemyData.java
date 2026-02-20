package com.betsoft.casino.mp.bgsectorx.model.math;

public class EnemyData {
    private String name;
    private int pay;
    private double Pkill;
    private int minpay;
    private int maxpay;
    private boolean enabled;

    public EnemyData(String name, int pay, double pkill, int minPay, int maxPay, boolean enabled) {
        this.name = name;
        this.pay = pay;
        this.Pkill = Pkill;
        this.minpay = minPay;
        this.maxpay = maxPay;
        this.enabled = enabled;
    }

    public int getMinPay() {
        return minpay;
    }

    public void setMinPay(int minPay) {
        this.minpay = minPay;
    }

    public int getMaxPay() {
        return maxpay;
    }

    public void setMaxPay(int maxPay) {
        this.maxpay = maxPay;
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

    public double getPkill() {
        return Pkill;
    }

    public void setPkill(double pkill) {
        Pkill = pkill;
    }

    @Override
    public String toString() {
        return "EnemyData{" +
                "name='" + name + '\'' +
                ", pay=" + pay +
                ", Pkill=" + Pkill +
                ", minPay=" + minpay +
                ", maxPay=" + maxpay +
                ", enabled=" + enabled +
                '}';
    }
}
