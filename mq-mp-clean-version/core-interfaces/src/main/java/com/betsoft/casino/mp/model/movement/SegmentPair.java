package com.betsoft.casino.mp.model.movement;

import java.util.Objects;

public class SegmentPair {
    private final Point a1;
    private final Point a2;
    private final Point b1;
    private final Point b2;

    public SegmentPair(Point a1, Point a2, Point b1, Point b2) {
        this.a1 = a1;
        this.a2 = a2;
        this.b1 = b1;
        this.b2 = b2;
    }

    public Point getA1() {
        return a1;
    }

    public Point getA2() {
        return a2;
    }

    public Point getB1() {
        return b1;
    }

    public Point getB2() {
        return b2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SegmentPair that = (SegmentPair) o;
        return Objects.equals(a1, that.a1) &&
                Objects.equals(a2, that.a2) &&
                Objects.equals(b1, that.b1) &&
                Objects.equals(b2, that.b2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a1, a2, b1, b2);
    }

    @Override
    public String toString() {
        return "SegmentPair{" +
                "a1=" + a1 +
                ", a2=" + a2 +
                ", b1=" + b1 +
                ", b2=" + b2 +
                '}';
    }
}
