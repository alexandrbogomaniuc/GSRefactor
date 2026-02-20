package com.betsoft.casino.mp.maxcrashgame.model.math.config;

import com.betsoft.casino.mp.model.gameconfig.IGameConfig;
import java.util.Collections;
import java.util.Map;

public class GameConfig implements IGameConfig {
    private int initialTime;
    private double crashMultiplier;
    private double H;
    private String function;

    public GameConfig() {}

    public GameConfig(int initialTime, double crashMultiplier, double H, String function) {
        this.initialTime = initialTime;
        this.crashMultiplier = crashMultiplier;
        this.H = H;
        this.function = function;
    }

    public int getInitialTime() {
        return initialTime;
    }

    public double getCrashMultiplier() {
        return crashMultiplier;
    }

    public double getH() {
        return H;
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
                ", H=" + H +
                ", function=" + function +
                ']';
    }
}
