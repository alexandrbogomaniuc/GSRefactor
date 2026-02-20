package com.betsoft.casino.mp.sectorx.model.math.config;

import java.util.Map;

public class BossPays {
    private Map<Integer, Double> partialPays;
    private Map<Integer, Map<Integer, Double>> partialPayProb;
    private int defeatThreshold;

    public BossPays(Map<Integer, Map<Integer, Double>> partialPayProb, Map<Integer, Double> partialPays,
                    int defeatThreshold) {
        this.partialPayProb = partialPayProb;
        this.partialPays = partialPays;
        this.defeatThreshold = defeatThreshold;
    }

    public Map<Integer, Double> getPartialPays() {
        return partialPays;
    }

    public Map<Integer, Map<Integer, Double>> getPartialPayProb() {
        return partialPayProb;
    }

    public int getDefeatThreshold() {
        return defeatThreshold;
    }

    @Override
    public String toString() {
        return "BossPays" + "[" +
                ", partialPays=" + partialPays +
                ", partialPayProb=" + partialPayProb +
                ", defeatThreshold=" + defeatThreshold +
                ']';
    }
}
