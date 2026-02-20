package com.betsoft.casino.mp.maxblastchampions.model.math.config;

import com.betsoft.casino.mp.model.gameconfig.IGameConfig;
import java.util.Collections;
import java.util.Map;

public class GameConfig implements IGameConfig {
    private int initialTime;
    private double crashMultiplier;
    private double alpha;
    private String function;
    private int a;
    private int b;
    private double mcm;

    public GameConfig() {}

    public GameConfig(int initialTime, double crashMultiplier, double alpha, String function, int a, int b, double mcm) {
        this.initialTime = initialTime;
        this.crashMultiplier = crashMultiplier;
        this.alpha = alpha;
        this.function = function;
        this.a = a;
        this.b = b;
        this.mcm = mcm;
    }

    public int getInitialTime() {
        return initialTime;
    }

    public double getCrashMultiplier() {
        return crashMultiplier;
    }

    public double getAlpha() {
        return alpha;
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    public double getMcm() {
        return mcm;
    }

    public String getFunction() {
        return function;
    }

    @Override
    public Map<Integer, Integer> getWeaponPrices() {
        return Collections.EMPTY_MAP;
    }

    @Override
    public String toString() {
        return "GameConfig[" +
                "initialTime=" + initialTime +
                ", crashMultiplier=" + crashMultiplier +
                ", alpha=" + alpha +
                ", function=" + function +
                ", a=" + a +
                ", b=" + b +
                ", mcm=" + mcm +
                ']';
    }
}
