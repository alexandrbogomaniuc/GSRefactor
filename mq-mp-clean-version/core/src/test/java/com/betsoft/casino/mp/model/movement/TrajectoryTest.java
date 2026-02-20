package com.betsoft.casino.mp.model.movement;

import org.junit.Test;

import static org.junit.Assert.*;

public class TrajectoryTest {

    @Test
    public void parallelSegmentsShouldNotIntersect() {
        Trajectory t1 = new Trajectory(1).addPoint(1, 1, 0).addPoint(5, 5, 100);
        Trajectory t2 = new Trajectory(1).addPoint(2, 1, 0).addPoint(7, 6, 100);
        assertFalse(t1.intersects(t2, 10));
    }

    @Test
    public void crossedSegmentsShouldIntersect() {
        Trajectory t1 = new Trajectory(1).addPoint(1, 1, 0).addPoint(5, 5, 100);
        Trajectory t2 = new Trajectory(1).addPoint(5, 1, 0).addPoint(1, 5, 100);
        assertTrue(t1.intersects(t2, 10));
    }

    @Test
    public void equalSegmentsShouldIntersect() {
        Trajectory t1 = new Trajectory(1).addPoint(1, 1, 0).addPoint(5, 5, 100);
        assertTrue(t1.intersects(t1, 10));
    }

    @Test
    public void trajectoriesWithOpposingDirectionsShouldIntersect() {
        Trajectory t1 = new Trajectory(1).addPoint(10, 10, 0).addPoint(10, 20, 100).addPoint(30, 20, 200).addPoint(30, 30, 250);
        Trajectory t2 = new Trajectory(1).addPoint(40, 10, 0).addPoint(40, 20, 100).addPoint(20, 20, 200).addPoint(20, 30, 250);
        assertTrue(t1.intersects(t2, 10));
    }

    @Test
    public void sameTrajectoryWithDifferentTimingsShouldNotIntersect() {
        Trajectory t1 = new Trajectory(1).addPoint(10, 10, 0).addPoint(10, 20, 100).addPoint(30, 20, 200).addPoint(30, 30, 250);
        Trajectory t2 = new Trajectory(1).addPoint(10, 10, 110).addPoint(10, 20, 210).addPoint(30, 20, 310).addPoint(30, 30, 360);
        assertFalse(t1.intersects(t2, 10));
    }


}