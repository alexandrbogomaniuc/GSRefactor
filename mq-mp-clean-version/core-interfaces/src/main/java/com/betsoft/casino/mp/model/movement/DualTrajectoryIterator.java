package com.betsoft.casino.mp.model.movement;

import java.util.Iterator;

public class DualTrajectoryIterator implements Iterator<SegmentPair> {
    private final Iterator<Point> first;
    private final Iterator<Point> second;

    private Point a1;
    private Point a2;
    private Point b1;
    private Point b2;

    private SegmentPair next;

    public DualTrajectoryIterator(Trajectory first, Trajectory second) {
        this.first = first.getPoints().iterator();
        this.second = second.getPoints().iterator();
        a1 = this.first.next();
        a2 = this.first.next();
        b1 = this.second.next();
        b2 = this.second.next();
        skipFirstSegments();
    }

    public boolean hasNext() {
        return next != null;
    }

    public SegmentPair next() {
        SegmentPair current = next;
        if (next != null) {
            if (a2.getTime() > b2.getTime()) {
                if (second.hasNext()) {
                    nextB();
                } else {
                    next = null;
                }
            } else if (a2.getTime() < b2.getTime()) {
                if (first.hasNext()) {
                    nextA();
                } else {
                    next = null;
                }
            } else if (first.hasNext()) {
                nextA();
            } else if (second.hasNext()) {
                nextB();
            } else {
                next = null;
            }
        }
        return current;
    }

    private void skipFirstSegments() {
        while (a1.getTime() > b2.getTime() && second.hasNext()) {
            b1 = b2;
            b2 = second.next();
        }
        while (b1.getTime() > a2.getTime() && first.hasNext()) {
            a1 = a2;
            a2 = first.next();
        }
        if (MathUtils.isOverlaps(a1.getTime(), a2.getTime(), b1.getTime(), b2.getTime())) {
            next = new SegmentPair(a1, a2, b1, b2);
        }
    }

    private void nextA() {
        a1 = a2;
        a2 = first.next();
        next = new SegmentPair(a1, a2, b1, b2);
    }

    private void nextB() {
        b1 = b2;
        b2 = second.next();
        next = new SegmentPair(a1, a2, b1, b2);
    }
}
