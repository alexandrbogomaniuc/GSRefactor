package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IAward;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Award implements IAward, Serializable {
    private int seatId;
    private double score;
    private List<Integer> treasures = new ArrayList<>();
    private float qualifyWin;

    public Award(int seatId, double score, float qualifyWin) {
        this.seatId = seatId;
        this.score = score;
        this.qualifyWin = qualifyWin;
    }

    @Override
    public void addTreasure(int treasureId) {
        this.treasures.add(treasureId);
    }

    @Override
    public int getSeatId() {
        return seatId;
    }

    @Override
    public double getPoints() {
        return score;
    }

    @Override
    public List<Integer> getTreasures() {
        return treasures;
    }

    @Override
    public float getQualifyWin() {
        return qualifyWin;
    }

    @Override
    public void setQualifyWin(float qualifyWin) {
        this.qualifyWin = qualifyWin;
    }

    @Override
    public String toString() {
        return "Award[" +
                "seatId=" + seatId +
                ", score=" + score +
                ", treasures=" + treasures +
                ", qualifyWin=" + qualifyWin +
                ']';
    }
}
