package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.IExperience;
import com.betsoft.casino.mp.model.ITreasure;

import java.util.ArrayList;
import java.util.List;

/**
 * User: flsh
 * Date: 14.02.19.
 */
public class Award {
    private int seatId;
    private IExperience points;
    private List<ITreasure> treasures = new ArrayList<>();
    private float qualifyWin;
    private double xp;

    public Award(int seatId, IExperience points) {
        this.seatId = seatId;
        this.points = points;
    }

    public int getSeatId() {
        return seatId;
    }

    public void addPoints(double points) {
        this.points.add(points);
    }

    public void addTreasure(ITreasure treasure) {
        treasures.add(treasure);
    }

    public IExperience getPoints() {
        return points;
    }

    public double getXp() {
        return xp;
    }

    public void setXp(double xp) {
        this.xp = xp;
    }

    public void scalePoints(double scale) {
        points.multiply(scale);
    }

    public List<ITreasure> getTreasures() {
        return treasures;
    }

    public float getQualifyWin() {
        return qualifyWin;
    }

    public void setQualifyWin(float qualifyWin) {
        this.qualifyWin = qualifyWin;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Award [");
        sb.append("seatId=").append(seatId);
        sb.append(", points=").append(points);
        sb.append(", treasures=").append(treasures);
        sb.append(", qualifyWin=").append(qualifyWin);
        sb.append(", xp=").append(xp);
        sb.append(']');
        return sb.toString();
    }
}
