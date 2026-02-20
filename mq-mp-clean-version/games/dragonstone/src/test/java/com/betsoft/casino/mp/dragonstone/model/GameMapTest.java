package com.betsoft.casino.mp.dragonstone.model;

import com.betsoft.casino.mp.common.Coords;
import com.betsoft.casino.mp.dragonstone.model.math.EnemyRange;
import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.model.movement.*;
import org.junit.Before;
import org.junit.Test;
import org.kynosarges.tektosyne.geometry.PointI;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class GameMapTest {

    private GameMapStore gameMapStore;

    @Before
    public void setUp() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test
    public void testAddPointsFromOldTrajectoryWithoutTeleport() {
        List<Point> points = new ArrayList<>();
        points.add(new FreezePoint(25, 25, 1000));
        points.add(new FreezePoint(25, 25, 4000));

        Trajectory trajectory = new Trajectory(4.0f)
                .addPoint(20, 20, 500)
                .addPoint(27, 27, 1200)
                .addPoint(33, 33, 1800)
                .addPoint(36, 36, 2100);
        Enemy enemy = new Enemy(1, null, 1, trajectory, null, -1, null);

        GameMap map = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getMap(301));
        map.addPointsFromOldTrajectoryWithTeleport(points, 1000, 3000, enemy);

        List<Point> expected = new ArrayList<>();
        expected.add(new Point(20, 20, 500));
        expected.add(new FreezePoint(27, 27, 1000));
        expected.add(new FreezePoint(27, 27, 4000));
        expected.add(new Point(27, 27, 4200));
        expected.add(new Point(33, 33, 4800));
        expected.add(new Point(36, 36, 5100));

        assertEquals(expected, points);
    }

    @Test
    public void testAddPointsFromOldTrajectoryWithTeleportBetweenFirstAndSecondPoints() {
        List<Point> points = new ArrayList<>();
        points.add(new FreezePoint(25, 25, 1000));
        points.add(new FreezePoint(25, 25, 4000));

        Trajectory trajectory = new Trajectory(4.0f)
                .addPoint(new TeleportPoint(20, 20, 500, false))
                .addPoint(20, 20, 1200)
                .addPoint(36, 36, 1800)
                .addPoint(36, 36, 2100);
        Enemy enemy = new Enemy(1, null, 1, trajectory, null, -1, null);

        GameMap map = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getMap(301));
        map.addPointsFromOldTrajectoryWithTeleport(points, 1000, 3000, enemy);

        List<Point> expected = new ArrayList<>();
        expected.add(new TeleportPoint(20, 20, 500, false));
        expected.add(new FreezePoint(20, 20, 1000));
        expected.add(new FreezePoint(20, 20, 4000));
        expected.add(new Point(20, 20, 4200));
        expected.add(new Point(36, 36, 4800));
        expected.add(new Point(36, 36, 5100));

        assertEquals(expected, points);
    }

    @Test
    public void testAddPointsFromOldTrajectoryWithTeleportBetweenSecondAndThirdPoints() {
        List<Point> points = new ArrayList<>();
        points.add(new FreezePoint(25, 25, 1000));
        points.add(new FreezePoint(25, 25, 4000));

        Trajectory trajectory = new Trajectory(4.0f)
                .addPoint(new TeleportPoint(20, 20, 500, false))
                .addPoint(20, 20, 1200)
                .addPoint(36, 36, 1800)
                .addPoint(36, 36, 2100);
        Enemy enemy = new Enemy(1, null, 1, trajectory, null, -1, null);

        GameMap map = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getMap(301));
        map.addPointsFromOldTrajectoryWithTeleport(points, 1500, 3000, enemy);

        List<Point> expected = new ArrayList<>();
        expected.add(new TeleportPoint(20, 20, 500, false));
        expected.add(new Point(20, 20, 1200));
        expected.add(new FreezePoint(36, 36, 1500));
        expected.add(new FreezePoint(36, 36, 4500));
        expected.add(new Point(36, 36, 4800));
        expected.add(new Point(36, 36, 5100));

        assertEquals(expected, points);
    }

    @Test
    public void testAddPointsFromOldTrajectoryWithTeleportBetweenThirdAndForthPoints() {
        List<Point> points = new ArrayList<>();
        points.add(new FreezePoint(25, 25, 1000));
        points.add(new FreezePoint(25, 25, 4000));

        Trajectory trajectory = new Trajectory(4.0f)
                .addPoint(new TeleportPoint(20, 20, 500, false))
                .addPoint(20, 20, 1200)
                .addPoint(36, 36, 1800)
                .addPoint(36, 36, 2100);
        Enemy enemy = new Enemy(1, null, 1, trajectory, null, -1, null);

        GameMap map = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getMap(301));
        map.addPointsFromOldTrajectoryWithTeleport(points, 2000, 3000, enemy);

        List<Point> expected = new ArrayList<>();
        expected.add(new TeleportPoint(20, 20, 500, false));
        expected.add(new Point(20, 20, 1200));
        expected.add(new Point(36, 36, 1800));
        expected.add(new FreezePoint(36, 36, 2000));
        expected.add(new FreezePoint(36, 36, 5000));
        expected.add(new Point(36, 36, 5100));

        assertEquals(expected, points);
    }

    @Test
    public void testRepeatedFreeze() {
        Trajectory trajectory = new Trajectory(4.0f)
                .addPoint(new TeleportPoint(20, 20, 500, false))
                .addPoint(20, 20, 1200)
                .addPoint(36, 36, 1800)
                .addPoint(36, 36, 2100)
                .addPoint(48, 48, 7000);

        Enemy enemy = new Enemy(1, null, 1, trajectory, null, -1, null);

        GameMap map = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getMap(301));

        List<Point> points = new ArrayList<>();
        map.addPointsFromOldTrajectoryWithTeleport(points, 2000, 3000, enemy);

        enemy.setTrajectory(new Trajectory(enemy.getSpeed(), points));
        List<Point> points2 = new ArrayList<>();
        map.addPointsFromOldTrajectoryWithTeleport(points2, 3000, 3000, enemy);

        enemy.setTrajectory(new Trajectory(enemy.getSpeed(), points2));
        List<Point> points3 = new ArrayList<>();
        map.addPointsFromOldTrajectoryWithTeleport(points3, 6500, 3000, enemy);

        List<Point> expected = new ArrayList<>();
        expected.add(new TeleportPoint(20, 20, 500, false));
        expected.add(new Point(20, 20, 1200));
        expected.add(new Point(36, 36, 1800));
        expected.add(new FreezePoint(36, 36, 2000));
        expected.add(new FreezePoint(36, 36, 6000));
        expected.add(new Point(36, 36, 6100));
        expected.add(new FreezePoint(48, 48, 6500));
        expected.add(new FreezePoint(48, 48, 9500));
        expected.add(new Point(48, 48, 14000));

        System.out.println(points);
        System.out.println(points2);
        System.out.println(points3);

        assertEquals(expected, points3);
    }

    @Test
    public void testCutGargoyleTrajectoryAfterCurrentPhase() {
        Trajectory trajectory = new Trajectory(6.5f)
                .addPoint(new InvulnerablePoint(30, 30, 1000L))
                .addPoint(new Point(30, 30, 1500L))
                .addPoint(new Point(30, 30, 2500L))
                .addPoint(new Point(40, 40, 3000L))
                .addPoint(new TeleportPoint(40, 40, 3500L, true))
                .addPoint(new InvulnerablePoint(40, 40, 3500L))
                .addPoint(new InvulnerablePoint(40, 40, 4000L))
                .addPoint(new InvulnerablePoint(40, 40, 4500L))

                .addPoint(new InvulnerablePoint(50, 50, 5000L))
                .addPoint(new Point(50, 50, 5500L))
                .addPoint(new Point(50, 50, 6500L))
                .addPoint(new Point(60, 60, 7000L))
                .addPoint(new TeleportPoint(60, 60, 7500L, true))
                .addPoint(new InvulnerablePoint(60, 60, 7500L))
                .addPoint(new InvulnerablePoint(60, 60, 8000L))
                .addPoint(new InvulnerablePoint(60, 60, 8500L))

                .addPoint(new InvulnerablePoint(50, 50, 8500L))
                .addPoint(new Point(50, 50, 9500L))
                .addPoint(new Point(50, 50, 10500L))
                .addPoint(new Point(60, 60, 11000L))
                .addPoint(new TeleportPoint(60, 60, 11500L, true))
                .addPoint(new InvulnerablePoint(60, 60, 11500L))
                .addPoint(new InvulnerablePoint(60, 60, 12000L))
                .addPoint(new InvulnerablePoint(60, 60, 12500L));

        Enemy gargoyle = new Enemy(1L, null, 1, trajectory, null, -1, null);
        GameMap map = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getMap(601));

        verifyTrajectory(map.cutGargoyleTrajectoryAfterCurrentPhase(500, gargoyle),
                8, trajectory.getPoints().get(0), trajectory.getPoints().get(7));
        verifyTrajectory(map.cutGargoyleTrajectoryAfterCurrentPhase(3000, gargoyle),
                8, trajectory.getPoints().get(0), trajectory.getPoints().get(7));
        verifyTrajectory(map.cutGargoyleTrajectoryAfterCurrentPhase(3500, gargoyle),
                8, trajectory.getPoints().get(0), trajectory.getPoints().get(7));
        verifyTrajectory(map.cutGargoyleTrajectoryAfterCurrentPhase(4100, gargoyle),
                16, trajectory.getPoints().get(0), trajectory.getPoints().get(15));
        verifyTrajectory(map.cutGargoyleTrajectoryAfterCurrentPhase(5001, gargoyle),
                8, trajectory.getPoints().get(8), trajectory.getPoints().get(15));
        verifyTrajectory(map.cutGargoyleTrajectoryAfterCurrentPhase(10000, gargoyle),
                8, trajectory.getPoints().get(16), trajectory.getPoints().get(23));
        verifyTrajectory(map.cutGargoyleTrajectoryAfterCurrentPhase(15000, gargoyle),
                8, trajectory.getPoints().get(16), trajectory.getPoints().get(23));
    }

    private void verifyTrajectory(Trajectory trajectory, int expectedSize, Point firstPoint, Point lastPoint) {
        assertEquals(expectedSize, trajectory.getPoints().size());
        assertEquals(firstPoint, trajectory.getPoints().get(0));
        assertEquals(lastPoint, trajectory.getLastPoint());
    }

    @Test
    public void cantFreezeWithFirstFixedPoint() {
        GameMap map = new GameMap();
        Trajectory trajectory = new Trajectory(1)
                .addPoint(new InvulnerableFixedPoint(10, 10, 1000L))
                .addPoint(new Point(20, 20, 2000L))
                .addPoint(new Point(30, 30, 3000L));
        PointI location = new PointI(15, 15);
        assertEquals(trajectory.getPoints(), map.freezeWithFixedPoint(1500, trajectory, 3000, location));
    }

    @Test
    public void freezeNearFixedPoint() {
        GameMap map = new GameMap();
        Trajectory trajectory = new Trajectory(1)
                .addPoint(new Point(10, 10, 1000L))
                .addPoint(new InvulnerableFixedPoint(20, 20, 2000L))
                .addPoint(new Point(30, 30, 3000L));
        PointI location = new PointI(15, 15);
        List<Point> expected = new ArrayList<>();
        expected.add(new FreezePoint(15, 15, 1500L));
        expected.add(new FreezePoint(15, 15, 2000L));
        expected.add(new InvulnerableFixedPoint(20, 20, 2000L));
        expected.add(new Point(30, 30, 3000L));
        assertEquals(expected, map.freezeWithFixedPoint(1500, trajectory, 3000, location));
    }

    @Test
    public void freezeWithSpaceBeforeFixedPoint() {
        GameMap map = new GameMap();
        Trajectory trajectory = new Trajectory(1)
                .addPoint(new Point(10, 10, 1000L))
                .addPoint(new InvulnerableFixedPoint(20, 20, 5000L))
                .addPoint(new Point(30, 30, 6000L));
        PointI location = new PointI(15, 15);
        List<Point> expected = new ArrayList<>();
        expected.add(new FreezePoint(15, 15, 1500L));
        expected.add(new FreezePoint(15, 15, 4500L));
        expected.add(new InvulnerableFixedPoint(20, 20, 5000L));
        expected.add(new Point(30, 30, 6000L));
        assertEquals(expected, map.freezeWithFixedPoint(1500, trajectory, 3000, location));
    }

    @Test
    public void freezeWithSpaceBeforeFixedPointAndFreezePoints() {
        GameMap map = new GameMap();
        Trajectory trajectory = new Trajectory(1)
                .addPoint(new FreezePoint(15, 15, 1500L))
                .addPoint(new FreezePoint(15, 15, 4500L))
                .addPoint(new Point(18, 18, 4800L))
                .addPoint(new InvulnerableFixedPoint(20, 20, 5000L))
                .addPoint(new Point(30, 30, 6000L));
        PointI location = new PointI(15, 15);
        List<Point> expected = new ArrayList<>();
        expected.add(new FreezePoint(15, 15, 2500L));
        expected.add(new FreezePoint(15, 15, 5000L));
        expected.add(new Point(18, 18, 5000L));
        expected.add(new InvulnerableFixedPoint(20, 20, 5000L));
        expected.add(new Point(30, 30, 6000L));
        assertEquals(expected, map.freezeWithFixedPoint(2500, trajectory, 3000, location));
    }

    @Test
    public void testCoords() {
        GameMap map = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getMap(301));
        Coords coords = map.getCoords();
        double x = 39;
        double y = 8.49;
        System.out.println(coords.toScreenX(x + 0.5, y + 0.5) + " " + coords.toScreenY(x + 0.5, y + 0.5));
    }
}
