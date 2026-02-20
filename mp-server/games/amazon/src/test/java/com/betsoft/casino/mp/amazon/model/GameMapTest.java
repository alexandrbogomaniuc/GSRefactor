package com.betsoft.casino.mp.amazon.model;

import com.betsoft.casino.mp.amazon.model.math.EnemyRange;
import com.betsoft.casino.mp.amazon.model.math.EnemyType;
import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.common.WaypointTrajectoryGenerator;
import com.betsoft.casino.mp.common.math.MathEnemy;
import com.betsoft.casino.mp.model.movement.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameMapTest {

    private GameMapStore gameMapStore;

    @Before
    public void setUp() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test
    public void testSpawnExplodedFrogs() {
        GameMap map = new GameMap(EnemyRange.BaseEnemies, gameMapStore.getMap(301));
        Enemy enemy = new Enemy(1L,
                new EnemyClass(2L, (short) 2, (short) 2, "Expoder", 100L, 4.0f, new ArrayList<>(), EnemyType.EXPLODER),
                1,
                new Trajectory(4.0f, Arrays.asList(
                        new Point(71.0, 75.0, 1586254306L),
                        new Point(71.0, 45.0, 1586254311L),
                        new Point(27.0, 45.0, 1586254318L),
                        new Point(27.0, 55.0, 1586254320L),
                        new Point(77.0, 55.0, 1586254328L),
                        new Point(77.0, 42.0, 1586254330L),
                        new Point(27.0, 42.0, 1586254339L),
                        new Point(27.0, 55.0, 1586254341L),
                        new Point(78.0, 55.0, 1586254349L),
                        new Point(91.0, 55.0, 1586254352L))),
                new MathEnemy(1, "test", 1, 100),
                -1,
                new ArrayList<>());


        for (long time = 1586254306L; time < 1586254352L; time++) {
            map.spawnExplodedFrogs(time, enemy);
        }
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

        GameMap map = new GameMap(EnemyRange.BaseEnemies, gameMapStore.getMap(301));
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

        GameMap map = new GameMap(EnemyRange.BaseEnemies, gameMapStore.getMap(301));
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

        GameMap map = new GameMap(EnemyRange.BaseEnemies, gameMapStore.getMap(301));
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

        GameMap map = new GameMap(EnemyRange.BaseEnemies, gameMapStore.getMap(301));
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

        GameMap map = new GameMap(EnemyRange.BaseEnemies, gameMapStore.getMap(301));

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
}
