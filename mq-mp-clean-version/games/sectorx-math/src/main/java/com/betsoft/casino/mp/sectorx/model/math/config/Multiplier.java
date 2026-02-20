package com.betsoft.casino.mp.sectorx.model.math.config;

import com.betsoft.casino.mp.model.IWeight;

public class Multiplier implements IWeight {
    int mul;
    int weight;

    public Multiplier(int mul, int weight) {
        this.mul = mul;
        this.weight = weight;
    }

    public int getMul() {
        return mul;
    }

    public void setMul(int mul) {
        this.mul = mul;
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

        Multiplier that = (Multiplier) o;

        if (mul != that.mul) return false;
        return weight == that.weight;
    }

    @Override
    public int hashCode() {
        int result = mul;
        result = 31 * result + weight;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Multiplier{");
        sb.append("mul=").append(mul);
        sb.append(", weight=").append(weight);
        sb.append('}');
        return sb.toString();
    }
}
