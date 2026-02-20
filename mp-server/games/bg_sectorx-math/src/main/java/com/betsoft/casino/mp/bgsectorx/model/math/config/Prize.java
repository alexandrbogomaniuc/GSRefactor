package com.betsoft.casino.mp.bgsectorx.model.math.config;

import com.betsoft.casino.mp.model.IWeight;

public class Prize implements IWeight {
    int pay;
    int weight;

    public Prize(int pay, int weight) {
        this.pay = pay;
        this.weight = weight;
    }

    public int getPay() {
        return pay;
    }

    public void setPay(int pay) {
        this.pay = pay;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Prize prize = (Prize) o;

        if (pay != prize.pay) return false;
        return weight == prize.weight;
    }

    @Override
    public int hashCode() {
        int result = pay;
        result = 31 * result + weight;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Prize{");
        sb.append("pay=").append(pay);
        sb.append(", weight=").append(weight);
        sb.append('}');
        return sb.toString();
    }
}
