package com.betsoft.casino.mp.bgdragonstone.model.math;

import com.betsoft.casino.mp.bgdragonstone.model.math.config.GameConfig;

public class DragonStone {
    public static double getDropProbability(GameConfig config, int seatsNumber) {
        return config.getFragments().getHitFrequency().get(seatsNumber);
    }
}
