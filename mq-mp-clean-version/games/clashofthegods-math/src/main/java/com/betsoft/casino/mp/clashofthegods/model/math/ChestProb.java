package com.betsoft.casino.mp.clashofthegods.model.math;

import com.betsoft.casino.mp.model.IChestProb;
import java.util.Arrays;

public class ChestProb implements IChestProb {

    private static final int sizes[] = {20, 50, 80, 120, 180, 250, 500};

    private final double chance;
    private final double[] probabilities;

    public ChestProb(double chance, double[] probabilities) {
        this.chance = chance;
        this.probabilities = probabilities;
    }

    public int[] getSizes() {
        return sizes;
    }

    @Override
    public double getChance() {
        return chance;
    }

    @Override
    public double[] getProbabilities() {
        return probabilities;
    }

    @Override
    public String toString() {
        return "ChestProb{" +
                "chance=" + chance +
                ", probabilities=" + Arrays.toString(probabilities) +
                '}';
    }
}
