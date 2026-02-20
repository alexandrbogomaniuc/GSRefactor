package com.betsoft.casino.mp.movement.common;

import java.util.Objects;

public class Step {
    private final double distance;
    private final int angle;

    public Step(double distance, int angle) {
        this.distance = distance;
        this.angle = angle;
    }

    public double getDistance() {
        return distance;
    }

    public int getAngle() {
        return angle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Step step = (Step) o;
        return Double.compare(step.distance, distance) == 0 && angle == step.angle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(distance, angle);
    }

    @Override
    public String toString() {
        return "Step{" +
                "distance=" + distance +
                ", angle=" + angle +
                '}';
    }
}
