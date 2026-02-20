package com.betsoft.casino.mp.model;

import java.util.List;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IAward {
    void addTreasure(int treasureId);

    int getSeatId();

    double getPoints();

    List<Integer> getTreasures();

    float getQualifyWin();

    void setQualifyWin(float qualifyWin);
}
