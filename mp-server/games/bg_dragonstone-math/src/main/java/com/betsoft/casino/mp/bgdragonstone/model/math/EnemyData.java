package com.betsoft.casino.mp.bgdragonstone.model.math;

import java.util.Map;

public class EnemyData {
    private final int payout;
    private final double PSWDrop;
    private final double PSlotDrop;
    private final Map<Integer, Double> killProbabilities;
    private boolean enabled;

    public EnemyData(int payout, double PSWDrop, double PSlotDrop, Map<Integer, Double> killProbabilities, boolean enabled) {
        this.payout = payout;
        this.PSWDrop = PSWDrop;
        this.PSlotDrop = PSlotDrop;
        this.killProbabilities = killProbabilities;
        this.enabled = enabled;
    }

    public int getPayout() {
        return payout;
    }

    public double getPSWDrop() {
        return PSWDrop;
    }

    public double getPSlotDrop() {
        return PSlotDrop;
    }

    public Map<Integer, Double> getKillProbabilities() {
        return killProbabilities;
    }

    public Double getKillProbability(int weaponId) {
        return killProbabilities.get(weaponId);
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "EnemyData{" +
                "payout=" + payout +
                ", PSWDrop=" + PSWDrop +
                ", PSlotDrop=" + PSlotDrop +
                ", killProbabilities=" + killProbabilities +
                ", enabled=" + enabled +
                '}';
    }
}
