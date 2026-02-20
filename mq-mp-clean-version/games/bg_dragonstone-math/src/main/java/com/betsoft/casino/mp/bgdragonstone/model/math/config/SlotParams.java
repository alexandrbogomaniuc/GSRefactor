package com.betsoft.casino.mp.bgdragonstone.model.math.config;

import java.util.Arrays;
import java.util.Map;

public class SlotParams {
    private int[] pays;
    private int spins;
    private int[][] reels;

    public SlotParams(int[] pays, int spins, int[][] reels) {
        this.pays = pays;
        this.spins = spins;
        this.reels = reels;
    }

    public int[][] getReels() {
        return reels;
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

    @Override
    public String toString() {
        return "SlotParams{" +
                "pays=" + Arrays.toString(pays) +
                ", spins=" + spins +
                ", reels=" + Arrays.toString(reels)  +
                '}';
    }
}
