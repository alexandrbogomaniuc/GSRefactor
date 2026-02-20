package com.betsoft.casino.mp.model;

import com.esotericsoftware.kryo.KryoSerializable;

import java.io.Serializable;
import java.util.Map;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IPlayerStats extends KryoSerializable, Serializable {
    void addRoundStats(IPlayerStats roundStats);

    double addScore(double deltaScore);

    Experience getScore();

    void setKills(Map<Integer, Long> kills);

    Map<Integer, Long> getKills();

    long getKillsCount();

    void setTreasures(Map<Integer, Long> treasures);

    Map<Integer, Long> getTreasures();

    long getTreasuresCount();

    int getVersion();

    void incrementVersion();

    int getRounds();

    void setRounds(int rounds);

    void addRound();
}
