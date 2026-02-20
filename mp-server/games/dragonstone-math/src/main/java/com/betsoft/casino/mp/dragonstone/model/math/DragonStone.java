package com.betsoft.casino.mp.dragonstone.model.math;

import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfig;

public class DragonStone {
    public static final int FRAGMENTS = 8;

    public static double getDropProbability(GameConfig config, int weaponTypeId, int betLevel) {
        double averageNumberOfTargets = MathData.getAverageDamageForWeapon(config, weaponTypeId);
        return 1.0 / getHitFrequency(config, weaponTypeId, betLevel) / averageNumberOfTargets;
    }

    public static double getHitFrequency(GameConfig config, int weaponTypeId, int betLevel) {
        return config.getFragments().getHitFrequency().get(weaponTypeId).get(betLevel);
    }
}
