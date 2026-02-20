package com.betsoft.casino.mp.bgsectorx.model.math.config;

import com.betsoft.casino.mp.bgsectorx.model.math.TrajectorySector;

import java.util.List;

public class PredefinedPathParam {
    private final int idx;
    private final List<PredefinedPoint> trajectoryPoints;
    private final int weight;
    private final TrajectoryType trajectoryType;
    private final TrajectorySector trajectorySector;
    private final double rot;

    public PredefinedPathParam(int idx, List<PredefinedPoint> trajectoryPoints, int weight, TrajectoryType trajectoryType, TrajectorySector trajectorySector, double rot) {
        this.idx = idx;
        this.trajectoryPoints = trajectoryPoints;
        this.weight = weight;
        this.trajectoryType = trajectoryType;
        this.trajectorySector = trajectorySector;
        this.rot = rot;
    }

    public TrajectorySector getTrajectorySector() {
        return trajectorySector;
    }

    public int getIdx() {
        return idx;
    }

    public List<PredefinedPoint> getTrajectoryPoints() {
        return trajectoryPoints;
    }

    public int getWeight() {
        return weight;
    }

    public TrajectoryType getTrajectoryType() {
        return trajectoryType;
    }

    public double getRot() {
        return rot;
    }

    @Override
    public String toString() {
        return "PredefinedPathParam{" +
                "trajectoryPoints=" + trajectoryPoints +
                ", weight=" + weight +
                ", trajectoryType=" + trajectoryType +
                ", trajectorySector=" + trajectorySector +
                ", rot=" + rot +
                '}';
    }
}
