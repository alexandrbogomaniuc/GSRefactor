package com.betsoft.casino.mp.amazon.model.math;

import java.util.HashMap;
import java.util.Map;

import static com.betsoft.casino.mp.amazon.model.math.SwarmType.*;

public class SwarmLimits {

    private static final Map<SwarmType, Integer> limits = new HashMap<>();

    static {
        limits.put(TRIPLE_SNAKE, 3);
        limits.put(ANT_SCENARIO, 2);
        limits.put(WASP_REGULAR, 2);
        limits.put(WASP_ORION, 1);
        limits.put(RUNNERS, 1);
    }

    public static int getLimit(SwarmType type) {
        return limits.get(type);
    }
}
