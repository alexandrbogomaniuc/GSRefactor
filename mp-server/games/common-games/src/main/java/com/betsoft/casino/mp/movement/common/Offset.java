package com.betsoft.casino.mp.movement.common;

import java.util.Objects;

public class Offset {
    private final double dx;
    private final double dy;
    private final long dt;

    public Offset(double dx, double dy, long dt) {
        this.dx = dx;
        this.dy = dy;
        this.dt = dt;
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    public long getDt() {
        return dt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Offset offset = (Offset) o;
        return Double.compare(offset.dx, dx) == 0 && Double.compare(offset.dy, dy) == 0 && dt == offset.dt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dx, dy, dt);
    }

    @Override
    public String toString() {
        return "Offset{" +
                "dx=" + dx +
                ", dy=" + dy +
                ", dt=" + dt +
                '}';
    }
}
