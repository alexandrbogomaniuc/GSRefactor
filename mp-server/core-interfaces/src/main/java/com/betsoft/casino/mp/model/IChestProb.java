package com.betsoft.casino.mp.model;

import com.abs.casino.common.util.RNG;

/**
 * User: flsh
 * Date: 15.02.19.
 */
public interface IChestProb {
    int[] getSizes();

    double getChance();

    double[] getProbabilities();

    static int roll(double[] probabilities) {
        double sum = 0.0;
        for (double probability : probabilities) {
            sum += probability;
        }
        double seed = RNG.rand();
        if (seed > sum) {
            return -1;
        }
        for (int i = 0; i < probabilities.length; i++) {
            seed -= probabilities[i];
            if (seed < 0) {
                return i;
            }
        }
        return probabilities.length - 1;
    }
}
