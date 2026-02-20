package com.betsoft.casino.mp.bgdragonstone.model.math.config;

import java.util.Map;

public class BossParams {
    private Map<Integer, Double> partialPays;
    private Map<Integer, Map<Integer, Double>> partialPayProb;
    private int defeatThreshold;

    public BossParams(Map<Integer, Map<Integer, Double>> partialPayProb, Map<Integer, Double> partialPays,
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
        return "BossParams{" +
                "partialPayProb=" + partialPayProb +
                ", partialPays=" + partialPays +
                ", defeatThreshold=" + defeatThreshold +
                '}';
    }
}
