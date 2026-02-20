package com.betsoft.casino.mp.maxcrashgame.model.math;

import java.util.*;

public class MathData {
    private static final Set<Integer> possibleBetLevels;

    static {
        possibleBetLevels = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(1, 2, 3, 5, 10)));
    }

    public static Set<Integer> getPossibleBetLevels() {
        return possibleBetLevels;
    }

}
