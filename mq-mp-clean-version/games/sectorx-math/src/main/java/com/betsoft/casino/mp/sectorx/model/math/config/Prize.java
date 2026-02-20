package com.betsoft.casino.mp.sectorx.model.math.config;

import com.betsoft.casino.mp.model.IWeight;

import java.util.Objects;

public class Prize implements IWeight {
    int pay;
    int minPay;
    int maxPay;
    int weight;

    public Prize(int pay, int minPay, int maxPay, int weight) {
        this.pay = pay;
        this.minPay = minPay;
        this.maxPay = maxPay;
        this.weight = weight;
    }

    public int getPay() {
        return pay;
    }

    public int getWeight() {
        return weight;
    }

    public int getMinPay() {
        return minPay;
    }

    public int getMaxPay() {
        return maxPay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prize prize = (Prize) o;
        return pay == prize.pay && minPay == prize.minPay && maxPay == prize.maxPay && weight == prize.weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pay, minPay, maxPay, weight);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Prize{");
        sb.append("pay=").append(pay);
        sb.append(", minPay=").append(minPay);
        sb.append(", maxPay=").append(maxPay);
        sb.append(", weight=").append(weight);
        sb.append('}');
        return sb.toString();
    }
}
