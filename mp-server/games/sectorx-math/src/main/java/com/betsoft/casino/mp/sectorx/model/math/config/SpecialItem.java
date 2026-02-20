package com.betsoft.casino.mp.sectorx.model.math.config;

import java.util.List;

public class SpecialItem {
    private String name;
    private List<Prize> prizes;
    private int pickWeight;
    public SpecialItem(String name, List<Prize> prizes, int pickWeight) {
        this.name = name;
        this.prizes = prizes;
        this.pickWeight = pickWeight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Prize> getPrizes() {
        return prizes;
    }

    public void setPrizes(List<Prize> prizes) {
        this.prizes = prizes;
    }

    public int getPickWeight() {
        return pickWeight;
    }

    public void setPickWeight(int pickWeight) {
        this.pickWeight = pickWeight;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpecialItem{");
        sb.append("name='").append(name).append('\'');
        sb.append(", prizes=").append(prizes);
        sb.append(", pickWeight=").append(pickWeight);
        sb.append('}');
        return sb.toString();
    }
}
