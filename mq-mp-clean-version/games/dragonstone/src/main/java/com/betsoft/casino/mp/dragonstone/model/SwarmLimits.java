package com.betsoft.casino.mp.dragonstone.model;

import java.util.HashMap;
import java.util.Map;

public class SwarmLimits {

    private static final Map<SwarmType, Integer> limits = new HashMap<>();

    static {
        limits.put(SwarmType.GOBLINS, 1);
        limits.put(SwarmType.SPIDERS, 1);
        limits.put(SwarmType.BATS, 1);
        limits.put(SwarmType.RATS, 3);
        limits.put(SwarmType.RAVENS, 3);
        limits.put(SwarmType.SKELETONS, 1);
        limits.put(SwarmType.ANGLE_SPIDERS, 1);
    }

    public static int getLimit(SwarmType type) {
        return limits.get(type);
    }
}
