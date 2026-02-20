package com.betsoft.casino.mp.model;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.HashMap;
import java.util.Map;

public class PlayerStats implements IPlayerStats {
    private static final byte VERSION = 0;

    private int version = 0;
    private Experience score = new Experience(0);
    private int rounds = 0;
    private Map<Integer, Long> kills = new HashMap<>();
    private Map<Integer, Long> treasures = new HashMap<>();

    @Override
    public void addRoundStats(IPlayerStats roundStats) {
        rounds++;
        score.add(roundStats.getScore());
    }

    @Override
    public double addScore(double deltaScore) {
        score.add(deltaScore);
        return deltaScore;
    }

    @Override
    public Experience getScore() {
        return score;
    }

    @Override
    public void setKills(Map<Integer, Long> kills) {
        this.kills = kills;
    }

    @Override
    public Map<Integer, Long> getKills() {
        return kills;
    }

    @Override
    public long getKillsCount() {
        return kills.values().stream().mapToLong(Number::longValue).sum();
    }

    @Override
    public void setTreasures(Map<Integer, Long> treasures) {
        this.treasures = treasures;
    }

    @Override
    public Map<Integer, Long> getTreasures() {
        return treasures;
    }

    @Override
    public long getTreasuresCount() {
        return treasures.values().stream().mapToLong(Number::longValue).sum();
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void incrementVersion() {
        version++;
    }

    @Override
    public int getRounds() {
        return rounds;
    }

    @Override
    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    @Override
    public void addRound() {
        rounds++;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(version, true);
        kryo.writeObject(output, score);
        output.writeInt(rounds, true);
        kryo.writeObject(output, kills);
        kryo.writeObject(output, treasures);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        version = input.readInt(true);
        score = kryo.readObject(input, Experience.class);
        rounds = input.readInt(true);
        kills = kryo.readObject(input, HashMap.class);
        treasures = kryo.readObject(input, HashMap.class);
    }

    @Override
    public String toString() {
        return "PlayerStats[" +
                "score=" + score +
                ", rounds=" + rounds +
                ", kills=" + kills +
                ", treasures=" + treasures +
                ']';
    }
}
