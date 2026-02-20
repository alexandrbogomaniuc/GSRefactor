package com.betsoft.casino.mp.bgdragonstone.model.math.config;

import java.util.Map;

public class FragmentParams {
    private int collectToSpawn;
    private Map<Integer, Double>  hitFrequency;

    public FragmentParams(int collectToSpawn, Map<Integer, Double>  hitFrequency) {
        this.collectToSpawn = collectToSpawn;
        this.hitFrequency = hitFrequency;
    }

    public int getCollectToSpawn() {
        return collectToSpawn;
    }

    public Map<Integer, Double>  getHitFrequency() {
        return hitFrequency;
    }

    @Override
    public String toString() {
        return "FragmentParams{" +
                "collectToSpawn=" + collectToSpawn +
                ", hitFrequency=" + hitFrequency +
                '}';
    }
}
