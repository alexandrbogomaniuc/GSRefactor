package com.betsoft.casino.mp.model.movement;

import org.junit.Test;

import static org.junit.Assert.*;

public class DualTrajectoryIteratorTest {

    @Test
    public void shouldNotReturnAnythingForTrajectoriesSeparatedByTime() {
        Trajectory first = new Trajectory(1)
                .addPoint(0, 0, 0)
                .addPoint(10, 10, 10);
        Trajectory second = new Trajectory(1)
                .addPoint(0, 0, 20)
                .addPoint(10, 10, 30);

        assertFalse(new DualTrajectoryIterator(first, second).hasNext());
    }

    @Test
    public void shouldReturnSingleEntryForTwoEqualSingleSegmentTrajectories() {
        Trajectory first = new Trajectory(1)
                .addPoint(0, 0, 0)
                .addPoint(10, 10, 10);
        Trajectory second = new Trajectory(1)
                .addPoint(0, 0, 0)
                .addPoint(10, 10, 10);

        DualTrajectoryIterator iterator = new DualTrajectoryIterator(first, second);
        assertTrue(iterator.hasNext());
        assertEquals(new SegmentPair(
                        new Point(0, 0, 0), new Point(10, 10, 10),
                        new Point(0, 0, 0), new Point(10, 10, 10)),
                iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void shouldPickNextSegmentWhenFirstTrajectoryEnded() {
        Trajectory first = new Trajectory(1)
                .addPoint(0, 0, 0)
                .addPoint(10, 10, 10);
        Trajectory second = new Trajectory(1)
                .addPoint(0, 0, 0)
                .addPoint(10, 10, 10)
                .addPoint(20, 20, 20);

        DualTrajectoryIterator iterator = new DualTrajectoryIterator(first, second);
        iterator.next();
        assertEquals(new SegmentPair(
                        new Point(0, 0, 0), new Point(10, 10, 10),
                        new Point(10, 10, 10), new Point(20, 20, 20)),
                iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void shouldPickNextSegmentWhenSecondTrajectoryEnded() {
        Trajectory first = new Trajectory(1)
                .addPoint(0, 0, 0)
                .addPoint(10, 10, 10)
                .addPoint(20, 20, 20);
        Trajectory second = new Trajectory(1)
                .addPoint(0, 0, 0)
                .addPoint(10, 10, 10);

        DualTrajectoryIterator iterator = new DualTrajectoryIterator(first, second);
        iterator.next();
        assertEquals(new SegmentPair(
                        new Point(10, 10, 10), new Point(20, 20, 20),
                        new Point(0, 0, 0), new Point(10, 10, 10)),
                iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void shouldIterateComplexTrajectory() {
        Point a30 = new Point(0, 0, 30);
        Point a50 = new Point(10, 10, 50);
        Point a80 = new Point(20, 20, 80);
        Point a90 = new Point(30, 30, 90);
        Point a120 = new Point(40, 40, 120);
        Point a140 = new Point(50, 50, 140);
        Point a170 = new Point(60, 60, 170);
        Trajectory first = new Trajectory(1)
                .addPoint(a30)
                .addPoint(a50)
                .addPoint(a80)
                .addPoint(a90)
                .addPoint(a120)
                .addPoint(a140)
                .addPoint(a170);

        Point b0 = new Point(0, 0, 0);
        Point b10 = new Point(0, 0, 10);
        Point b20 = new Point(0, 10, 20);
        Point b60 = new Point(10, 20, 60);
        Point b70 = new Point(20, 30, 70);
        Point b80 = new Point(30, 40, 80);
        Point b100 = new Point(40, 50, 100);
        Point b110 = new Point(50, 60, 110);
        Point b130 = new Point(60, 70, 130);
        Point b180 = new Point(70, 80, 180);
        Point b200 = new Point(80, 90, 200);
        Trajectory second = new Trajectory(1)
                .addPoint(b0)
                .addPoint(b10)
                .addPoint(b20)
                .addPoint(b60)
                .addPoint(b70)
                .addPoint(b80)
                .addPoint(b100)
                .addPoint(b110)
                .addPoint(b130)
                .addPoint(b180)
                .addPoint(b200);

        DualTrajectoryIterator iterator = new DualTrajectoryIterator(first, second);
        assertEquals(new SegmentPair(a30, a50, b20, b60), iterator.next());
        assertEquals(new SegmentPair(a50, a80, b20, b60), iterator.next());
        assertEquals(new SegmentPair(a50, a80, b60, b70), iterator.next());
        assertEquals(new SegmentPair(a50, a80, b70, b80), iterator.next());
        assertEquals(new SegmentPair(a80, a90, b70, b80), iterator.next());
        assertEquals(new SegmentPair(a80, a90, b80, b100), iterator.next());
        assertEquals(new SegmentPair(a90, a120, b80, b100), iterator.next());
        assertEquals(new SegmentPair(a90, a120, b100, b110), iterator.next());
        assertEquals(new SegmentPair(a90, a120, b110, b130), iterator.next());
        assertEquals(new SegmentPair(a120, a140, b110, b130), iterator.next());
        assertEquals(new SegmentPair(a120, a140, b130, b180), iterator.next());
        assertEquals(new SegmentPair(a140, a170, b130, b180), iterator.next());
        assertFalse(iterator.hasNext());
    }

}