package com.betsoft.casino.mp.model.movement;

public class InvulnerableFixedPoint extends InvulnerablePoint {

    public InvulnerableFixedPoint() {}

    public InvulnerableFixedPoint(double x, double y, long time) {
        super(x, y, time);
    }

    public boolean isFixed() {
        return true;
    }

    @Override
    public Point create(double x, double y, long time) {
        return new InvulnerablePoint(x, y, time);
    }

    @Override
    public String toString() {
        return "InvulnerableFixedPoint{" +
                "x=" + x +
                ", y=" + y +
                ", time=" + time +
                '}';
    }
}
