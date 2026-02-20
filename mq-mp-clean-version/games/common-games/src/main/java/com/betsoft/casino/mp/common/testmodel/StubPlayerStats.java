package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.Experience;
import com.betsoft.casino.mp.model.IExperience;
import com.betsoft.casino.mp.model.IPlayerStats;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.HashMap;
import java.util.Map;

public class StubPlayerStats implements IPlayerStats {
    private Map<Integer, Long> kills = new HashMap<>();
    private Map<Integer, Long> treasures = new HashMap<>();

    @Override
    public void addRoundStats(IPlayerStats roundStats) {

    }

    @Override
    public double addScore(double deltaScore) {
        return 0;
    }

    @Override
    public Experience getScore() {
        return new Experience();
    }

    @Override
    public Map<Integer, Long> getKills() {
        return new HashMap<>();
    }

    @Override
    public void setKills(Map<Integer, Long> kills) {

    }

    @Override
    public long getKillsCount() {
        return 0;
    }

    @Override
    public Map<Integer, Long> getTreasures() {
        return treasures;
    }

    @Override
    public void setTreasures(Map<Integer, Long> treasures) {
        this.treasures = treasures;
    }

    @Override
    public long getTreasuresCount() {
        return 0;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public void incrementVersion() {

    }

    @Override
    public int getRounds() {
        return 0;
    }

    @Override
    public void setRounds(int rounds) {

    }

    @Override
    public void addRound() {

    }

    @Override
    public void write(Kryo kryo, Output output) {
    }

    @Override
    public void read(Kryo kryo, Input input) {
    }
}
