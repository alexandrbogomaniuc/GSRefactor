package com.dgphoenix.casino.common.util;

import cern.jet.random.Exponential;
import cern.jet.random.Uniform;
import cern.jet.random.engine.DRand;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

public class RNG {
    private static final int DEFAULT_LOOP_LEN = 10;
    private static final MersenneTwister twister;
    private static final SecureRandom randomLoop;

    private static final RandomEngine randomEngine = new DRand();

    private static final Uniform uniform;

    static {
        long seed = System.nanoTime();
        int addSeed = 0;

        byte[] fileData = new byte[4];
        byte[] fileDataLoopSeed = new byte[20];

        boolean initComplete = false;

        DataInputStream dis = null;
        try {
            File file = new File("/dev/random");

            dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(fileData);

            ByteBuffer byteBuffer = ByteBuffer.wrap(fileData);
            IntBuffer ib = ((ByteBuffer) byteBuffer.rewind()).asIntBuffer();

            addSeed = ib.get(0);
            dis.readFully(fileDataLoopSeed);
            initComplete = true;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dis != null) {
                    dis.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        SecureRandom randomLoop_;
        try {
            randomLoop_ = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            randomLoop_ = new SecureRandom();
        }

        randomLoop = randomLoop_;

        if (initComplete)
            randomLoop.setSeed(fileDataLoopSeed);

        twister = new MersenneTwister(new Date(seed + addSeed));

        uniform = new Uniform(twister);
    }

    private RNG() {
        // Added private constructor to hide the implicit public one - to prevent instantiation
    }

    public static boolean nextBoolean() {
        randomLoop();
        return nextInt(2) == 1;
    }


    /**
     * returns random integer in the closed interval [Integer.MIN_VALUE,Integer.MAX_VALUE] (including Integer.MIN_VALUE and Integer.MAX_VALUE).
     *
     * @return int
     */
    public static int nextInt() {
        randomLoop();
        synchronized (twister) {
            return twister.nextInt();
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
        int rnd;
        randomLoop();
        synchronized (twister) {
            rnd = (int) (twister.raw() * intervalEnd);
        }
        return rnd;
    }

    public static int nextInt(int intervalEnd, boolean rndLoop) {
        randomLoop();
        int rnd = nextInt(intervalEnd);
        return rnd;
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
        int rnd;
        randomLoop();
        synchronized (twister) {
            rnd = (int) (twister.raw() * (intervalEnd - intervalBegin) + intervalBegin);
        }
        return rnd;
    }

    /**
     * Generates a random integer value based on the uniform distribution [minVal, maxVal] (inclusive).
     *
     * @return int
     */
    public static int nextIntUniform(int minVal, int maxVal) {
        synchronized (uniform) {
            return uniform.nextIntFromTo(minVal, maxVal);
        }
    }
    public static long nextLong() {
        long rnd;
        randomLoop();
        synchronized (twister) {
            rnd = twister.nextLong();
        }
        return rnd;
    }

    /**
     * returns random double in the interval [0, 1) (including 0, but not including 1).
     *
     * @return double
     */
    public static double rand() {
        double rnd;
        randomLoop();
        synchronized (twister) {
            rnd = twister.raw();
        }
        return rnd;
    }

    /**
     * returns random double in the interval [0, 1) (including 0, but not including 1).
     *
     * @param rndLoop loop
     * @return double
     */
    public static double rand(boolean rndLoop) {
        double rnd;
        randomLoop();
        synchronized (twister) {
            rnd = twister.raw();
        }
        return rnd;
    }

    /**
     * Returns random double value based on the uniform distribution
     * ensuring the result lies in the inclusive range [0, 1].
     *
     * Implementation Detail: To ensure that the result can potentially include both values Math.ulp(0d) and Math.ulp(1d)
     * are added to the interval in order to expand it
     *
     * @return double
     */
    public static double randUniform() {
        synchronized (uniform) {
            return uniform.nextDoubleFromTo(0 - Math.ulp(0d), 1 + Math.ulp(1d));
        }
    }

    /**
     * Generates a random double value based on the exponential distribution,
     * ensuring the result lies in the inclusive range [minVal, maxVal].
     *
     * <p>The rate (lambda) of the exponential distribution is computed based
     * on the mean of the provided range (i.e., (minVal + maxVal) / 2). As a result,
     * the average value of the generated numbers will be approximately the midpoint
     * of the range.</p>
     *
     * <p>Note: This method uses a rejection sampling approach, where numbers
     * are generated continuously until a value within the specified range is obtained.
     * Depending on the provided range and its relation to the underlying exponential distribution,
     * this method may exhibit variable performance.</p>
     *
     * @param minVal The minimum bound (inclusive) of the generated value.
     * @param maxVal The maximum bound (inclusive) of the generated value.
     * @return A random double value between [minVal, maxVal].
     */
    public static double randExponential(double minVal, double maxVal) {
        double meanVal = (minVal + maxVal) / 2.0;
        double lam = 1.0 / meanVal;

        synchronized (randomEngine) {
            Exponential exponential = new Exponential(lam, randomEngine);

            while (true) {
                double expVal = exponential.nextDouble();
                if (expVal >= minVal && expVal <= maxVal) {
                    return expVal;
                }
            }
        }
    }

    public static int getRandom(List<Integer> probabilities) {
        if (probabilities == null || probabilities.isEmpty()) {
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
            if (rnd >= start && rnd < start + probability) {
                return i;
            }
            start += probability;
            i++;
        }
        return i < probabilities.size() ? i : -1;
    }

    private static void randomLoop() {
        int loopLen = randomLoop.nextInt(DEFAULT_LOOP_LEN);
        synchronized (twister) {
            for (int i = 0; i < loopLen; i++) {
                twister.nextInt();
            }
        }
    }

}
