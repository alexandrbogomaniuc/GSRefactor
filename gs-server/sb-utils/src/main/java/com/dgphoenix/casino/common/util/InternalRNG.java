package com.dgphoenix.casino.common.util;

import cern.jet.random.engine.MersenneTwister;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created
 * Date: 20.11.2008
 * Time: 10:36:16
 */
public class InternalRNG {
    private static final int DEFAULT_LOOP_LEN = 10;
    private static final MersenneTwister twister = new MersenneTwister(new Date());

    /**
     * returns random integer in the closed interval [Integer.MIN_VALUE,Integer.MAX_VALUE] (including Integer.MIN_VALUE and Integer.MAX_VALUE).
     *
     * @return int
     */
    public static int nextInt() {
        synchronized (twister) {
            return twister.nextInt();
        }
    }

    public static boolean nextBoolean() {
        return nextInt(2) == 1;
    }

    public static long nextLong() {
        synchronized (twister) {
            return twister.nextLong();
        }
    }

    /**
     * returns random integer in the interval [0, intervalEnd) (including 0, but not including intervalEnd).
     *
     * @param intervalEnd end of interval
     * @return int
     */
    public static int nextInt(int intervalEnd) {
        if (intervalEnd < 2) {
            return 0;
        }
        synchronized (twister) {
            return (int) (twister.raw() * intervalEnd);
        }
    }

    public static long nextLong(long intervalEnd) {
        if (intervalEnd < 2) {
            return 0;
        }
        synchronized (twister) {
            return (long) (twister.raw() * intervalEnd);
        }
    }

    public static int nextInt(int intervalEnd, boolean rndLoop) {
        if (rndLoop) {
            randomLoop(DEFAULT_LOOP_LEN);
        }
        return nextInt(intervalEnd);
    }

    private static void randomLoop(int maxLoopLen) {
        int loopLen = InternalRNG.nextInt(maxLoopLen);
        for(int i=0;i<loopLen;i++) {
            nextInt();
        }
    }

    /**
     * returns random integer in the interval [intervalBegin, intervalEnd) (including intervalBegin, but not including intervalEnd).
     *
     * @param intervalBegin begin of interval
     * @param intervalEnd   end of interval
     * @return int
     */
    public static int nextInt(int intervalBegin, int intervalEnd) {
        if (intervalEnd <= intervalBegin) {
            return intervalBegin;
        }
        synchronized (twister) {
            return (int) (twister.raw() * (intervalEnd - intervalBegin) + intervalBegin);
        }
    }

    /**
     * returns random integer in the interval [intervalBegin, intervalEnd) (including intervalBegin, but not including intervalEnd).
     *
     * @param intervalBegin begin of interval
     * @param intervalEnd   end of interval
     * @return long
     */
    public static long nextInt(long intervalBegin, long intervalEnd) {
        if (intervalEnd <= intervalBegin) {
            return intervalBegin;
        }
        synchronized (twister) {
            return (long) (twister.raw() * (intervalEnd - intervalBegin) + intervalBegin);
        }
    }

    /**
     * returns random double in the interval [0, 1) (including 0, but not including 1).
     *
     * @return double
     */
    public static double rand() {
        synchronized (twister) {
            return twister.raw();
        }
    }

    /**
     * returns random double in the interval [0, 1) (including 0, but not including 1).
     *
     * @param rndLoop loop
     * @return double
     */
    public static double rand(boolean rndLoop) {
        if (rndLoop) {
            randomLoop(DEFAULT_LOOP_LEN);
        }
        synchronized (twister) {
            return twister.raw();
        }
    }

    public static int getRandom(List<Integer> probabilities) {
        if(probabilities == null || probabilities.isEmpty()) {
            throw new RuntimeException("empty probabilities list");
        }
        int probabilitySum = 0;
        for (Integer probability : probabilities) {
            probabilitySum += probability;
        }
        int rnd = nextInt(probabilitySum);
        int start = 0;
        int i = 0;
        for (Integer probability : probabilities) {
            if(rnd >= start && rnd <  start + probability) {
                return i;
            }
            start +=probability;
            i++;
        }
        return i < probabilities.size() ? i : -1;
    }
}
