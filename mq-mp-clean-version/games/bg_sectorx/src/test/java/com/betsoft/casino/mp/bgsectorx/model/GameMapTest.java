package com.betsoft.casino.mp.bgsectorx.model;

import com.betsoft.casino.mp.bgsectorx.model.math.EnemyRange;
import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.model.movement.FreezePoint;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.TeleportPoint;
import com.betsoft.casino.mp.model.movement.Trajectory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

        GameMap map = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getMap(1001));
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

        GameMap map = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getMap(1001));
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

        GameMap map = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getMap(1001));
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

        GameMap map = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getMap(1001));
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

        GameMap map = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getMap(1001));

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
