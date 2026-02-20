package com.betsoft.casino.mp.dragonstone.model.math.config;

import java.util.Arrays;
import java.util.Map;

public class SlotParams {
    private int[] pays;
    private int spins;
    private Map<Integer, Double> probabilityByWeapon;

    public SlotParams(int[] pays, int spins, Map<Integer, Double> probabilityByWeapon) {
        this.pays = pays;
        this.spins = spins;
        this.probabilityByWeapon = probabilityByWeapon;
    }

    public int[] getPays() {
        return pays;
    }

    public int getPay(int symbol) {
        return pays[symbol - 1];
    }

    public int getSpins() {
        return spins;
    }

    public double getProbabilityByWeapon(int weaponId) {
        return probabilityByWeapon.get(weaponId);
    }

    public Map<Integer, Double> getProbabilityByWeapon() {
        return probabilityByWeapon;
    }

    @Override
    public String toString() {
        return "SlotParams{" +
                "pays=" + Arrays.toString(pays) +
                ", spins=" + spins +
                ", probabilityByWeapon=" + probabilityByWeapon +
                '}';
    }
}
