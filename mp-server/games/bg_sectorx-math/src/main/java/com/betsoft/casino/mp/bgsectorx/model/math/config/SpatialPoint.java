package com.betsoft.casino.mp.bgsectorx.model.math.config;

public class SpatialPoint {
    private final double dx;
    private final double dy;

    public SpatialPoint(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    @Override
    public String toString() {
        return "SpatialPoint{" +
                "dx=" + dx +
                ", dy=" + dy +
                '}';
    }
}

