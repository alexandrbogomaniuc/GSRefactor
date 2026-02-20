package com.betsoft.casino.mp.common;

import org.junit.Before;
import org.junit.Test;
import org.kynosarges.tektosyne.geometry.PointI;

import java.io.DataInputStream;
import java.util.Collections;
import java.util.Objects;


import static org.junit.Assert.*;

public class MinStepTrajectoryGeneratorTest {

    private GameMapShape map;

    @Before
    public void setUp() throws Exception {
        map = new GameMapShape("map", new DataInputStream(Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("testMaps/rectPath.map"))));
    }

    @Test
    public void canWalk() {
        MinStepTrajectoryGenerator generator = new MinStepTrajectoryGenerator(map, new PointI(69, 30), 1.0, 20, 50);
        assertTrue(generator.canWalk(69, 30, 2, 39));
        assertTrue(generator.canWalk(30, 30, 1, 29));
        assertTrue(generator.canWalk(30, 59, 0, 39));
        assertTrue(generator.canWalk(69, 59, 3, 29));
    }

    @Test
    public void canRotate() {
        MinStepTrajectoryGenerator generator = new MinStepTrajectoryGenerator(map, new PointI(69, 30), 1.0, 20, 50);
        assertTrue(generator.canRotate(30, 30, 0, 39, 1));
        assertTrue(generator.canRotate(69, 30, 1, 29, 1));
        assertTrue(generator.canRotate(69, 59, 2, 39, 1));
        assertTrue(generator.canRotate(30, 59, 3, 29, 1));

        assertTrue(generator.canRotate(30, 30, 1, 29, -1));
        assertTrue(generator.canRotate(30, 59, 0, 39, -1));
        assertTrue(generator.canRotate(69, 59, 3, 29, -1));
        assertTrue(generator.canRotate(69, 30, 2, 39, -1));

        assertFalse(generator.canRotate(69, 59, 2, 38, 1));
        assertFalse(generator.canRotate(69, 59, 2, 40, 1));
    }

    @Test
    public void cantRotateWithTooLargeMinStep() {
        MinStepTrajectoryGenerator generator = new MinStepTrajectoryGenerator(map, new PointI(69, 30), 1.0, 35, 50);
        assertFalse(generator.canRotate(69, 59, 2, 39, 1));
    }

    @Test
    public void cantRotateWithTooSmallMaxStep() {
        MinStepTrajectoryGenerator generator = new MinStepTrajectoryGenerator(map, new PointI(69, 30), 1.0, 20, 35);
        assertFalse(generator.canRotate(69, 59, 2, 39, 1));
    }

    @Test
    public void canRotateWithExactLargeStep() {
        MinStepTrajectoryGenerator generator = new MinStepTrajectoryGenerator(map, new PointI(69, 30), 1.0, 29, 39);
        assertTrue(generator.canRotate(69, 59, 2, 39, 1));
    }

    @Test
    public void canRotateWithExactSmallStep() {
        MinStepTrajectoryGenerator generator = new MinStepTrajectoryGenerator(map, new PointI(69, 30), 1.0, 29, 39);
        assertTrue(generator.canRotate(69, 59, 3, 29, -1));
    }

    @Test
    public void canCalculateNextDir() {
        MinStepTrajectoryGenerator generator = new MinStepTrajectoryGenerator(map, new PointI(69, 30), 1.0, 20, 50);
        assertEquals(0, generator.nextDir(3, 1));
        assertEquals(1, generator.nextDir(0, 1));
        assertEquals(3, generator.nextDir(0, -1));
        assertEquals(1, generator.nextDir(2, -1));
    }

    @Test
    public void shouldNotThrowIfThereIsNoSpawnPointsAtLine() throws Exception {
        GameMapShape map = new GameMapShape("map", new DataInputStream(Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("testMaps/beach.map"))));
        MinStepTrajectoryGenerator generator = new MinStepTrajectoryGenerator(map, new PointI(26, 47), 1.0, 20, 50);
        assertEquals(-1, generator.getDistanceToSpawnPoint(26, 47, 0));
    }

    @Test
    public void shouldNotWalkOverWalls() throws Exception {
        GameMapShape map = new GameMapShape("map", new DataInputStream(Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("testMaps/202.map"))));
        MinStepTrajectoryGenerator generator = new MinStepTrajectoryGenerator(map, new PointI(71, 29), 1.0, 17, 22);
        assertEquals(Collections.singletonList(17), generator.getAvailableSteps(71, 29, 2));
    }

    @Test
    public void shouldNotJumpOverWalls() throws Exception {
        GameMapShape map = new GameMapShape("map", new DataInputStream(Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("testMaps/203.map"))));
        MinStepTrajectoryGenerator generator = new MinStepTrajectoryGenerator(map, new PointI(52, 28), 1.0, 17, 22);
        assertEquals(Collections.singletonList(17), generator.getAvailableSteps(52, 28, 0));
    }
}