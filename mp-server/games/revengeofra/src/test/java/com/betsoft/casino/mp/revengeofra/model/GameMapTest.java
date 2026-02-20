package com.betsoft.casino.mp.revengeofra.model;

import com.betsoft.casino.mp.common.GameMapShape;
import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.common.SwarmParams;
import com.betsoft.casino.mp.common.WaypointTrajectoryGenerator;
import com.betsoft.casino.mp.common.scenarios.SpawnGroup;
import com.betsoft.casino.mp.common.scenarios.SpawnScenario;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.movement.*;
import com.betsoft.casino.mp.revengeofra.model.math.EnemyRange;
import com.betsoft.casino.mp.revengeofra.model.math.EnemyType;
import org.junit.Before;
import org.junit.Test;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GameMapTest {

    private GameMapStore gameMapStore;

    @Before
    public void setUp() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test
    public void verifyThatAllPortalPredefinedTrajectoriesHasValidDistanceBetweenSpawnPoints() {
        for (int mapId : GameType.REVENGE_OF_RA.getMaps()) {
            GameMapShape map = gameMapStore.getMap(mapId);
            for (SpawnScenario scenario : map.getScenarios()) {
                for (SpawnGroup group : scenario.getGroups()) {
                    if (group.isFromPortal()) {
                        List<Point> points = group.getTrajectory().getPoints();
                        String message = "Map: " + mapId + ", Scenario: " + scenario.getId();
                        assertTrue(message, pointsOnTheSameLineAndHasDistance(points.get(0), points.get(1), 9));
                        assertTrue(message, pointsOnTheSameLineAndHasDistance(points.get(1), points.get(2), 3));
                    }
                }
            }
        }
    }

    private boolean pointsOnTheSameLineAndHasDistance(Point a, Point b, int distance) {
        return (a.getX() == b.getX() && Math.abs(a.getY() - b.getY()) == distance)
                || (a.getY() == b.getY() && Math.abs(a.getX() - b.getX()) == distance);
    }

    @Test
    public void verifyThatAllPortalSwarmParamsArePointedDown() {
        for (int mapId : GameType.REVENGE_OF_RA.getMaps()) {
            GameMapShape map = gameMapStore.getMap(mapId);
            for (SwarmParams params : map.getSwarmParams()) {
                if (params.isFromPortal()) {
                    String message = "Map: " + mapId + ", id: " + params.getId();
                    assertTrue(message, params.getAngle() > 190 && params.getAngle() < 350);
                }
            }
        }
    }

    @Test
    public void testPortalLeaveTrajectoryBeforePortal() {
        Trajectory trajectory = new Trajectory(4.0f, Arrays.asList(
                new PortalPoint(20, 20, 100, 1),
                new Point(30, 30, 200),
                new Point(50, 50, 300),
                new Point(70, 70, 400)));
        Enemy enemy = new Enemy(1L,
                new EnemyClass(2L, (short) 2, (short) 2, "Scarab", 100L, 4.0f, new ArrayList<>(), EnemyType.ENEMY_1),
                1,
                trajectory,
                null,
                -1,
                new ArrayList<>());
        GameMap map = new GameMap(EnemyRange.BaseEnemies, gameMapStore.getMap(501));
        Trajectory leaveTrajectory = map.generatePortalLeaveTrajectory(new WaypointTrajectoryGenerator(map.getMapShape()), enemy.getLocation(150).toPointI(), 150, enemy);
        assertEquals(new PortalPoint(20, 20, 100, 1), leaveTrajectory.getPoints().get(0));
        assertEquals(new Point(30, 30, 200), leaveTrajectory.getPoints().get(1));
        assertEquals(new Point(50, 50, 300), leaveTrajectory.getPoints().get(2));
        assertEquals(new Point(9, 50, 10150), leaveTrajectory.getPoints().get(3));
        assertEquals(4, leaveTrajectory.getPoints().size());
    }

    @Test
    public void testPortalLeaveTrajectoryAfterPortal() {
        Trajectory trajectory = new Trajectory(4.0f, Arrays.asList(
                new PortalPoint(20, 20, 100, 1),
                new Point(30, 30, 200),
                new Point(40, 40, 300),
                new Point(50, 50, 400),
                new Point(70, 70, 500)));
        Enemy enemy = new Enemy(1L,
                new EnemyClass(2L, (short) 2, (short) 2, "Scarab", 100L, 4.0f, new ArrayList<>(), EnemyType.ENEMY_1),
                1,
                trajectory,
                null,
                -1,
                new ArrayList<>());
        GameMap map = new GameMap(EnemyRange.BaseEnemies, gameMapStore.getMap(501));
        Trajectory leaveTrajectory = map.generatePortalLeaveTrajectory(new WaypointTrajectoryGenerator(map.getMapShape()), enemy.getLocation(350).toPointI(), 350, enemy);
        assertEquals(new Point(45, 45, 350), leaveTrajectory.getPoints().get(0));
        assertEquals(new Point(5, 45, 10350), leaveTrajectory.getPoints().get(1));
        assertEquals(2, leaveTrajectory.getPoints().size());
    }

    @Test
    public void testGetPortalEnemySpawnPoint() {
        GameMap map = new GameMap(EnemyRange.BaseEnemies, gameMapStore.getMap(501));

        assertEquals(new PointD(50, 50), map.getPortalEnemySpawnPoint(new PointI(50, 50), true, 0));
        assertEquals(new PointD(49.5, 49), map.getPortalEnemySpawnPoint(new PointI(50, 50), true, 1));
        assertEquals(new PointD(49.5, 51), map.getPortalEnemySpawnPoint(new PointI(50, 50), true, 2));
        assertEquals(new PointD(49, 50), map.getPortalEnemySpawnPoint(new PointI(50, 50), true, 3));
        assertEquals(new PointD(48.5, 49), map.getPortalEnemySpawnPoint(new PointI(50, 50), true, 4));
        assertEquals(new PointD(48.5, 51), map.getPortalEnemySpawnPoint(new PointI(50, 50), true, 5));
        assertEquals(new PointD(48, 50), map.getPortalEnemySpawnPoint(new PointI(50, 50), true, 6));

        assertEquals(new PointD(50, 50), map.getPortalEnemySpawnPoint(new PointI(50, 50), false, 0));
        assertEquals(new PointD(49, 49.5), map.getPortalEnemySpawnPoint(new PointI(50, 50), false, 1));
        assertEquals(new PointD(51, 49.5), map.getPortalEnemySpawnPoint(new PointI(50, 50), false, 2));
        assertEquals(new PointD(50, 49), map.getPortalEnemySpawnPoint(new PointI(50, 50), false, 3));
        assertEquals(new PointD(49, 48.5), map.getPortalEnemySpawnPoint(new PointI(50, 50), false, 4));
        assertEquals(new PointD(51, 48.5), map.getPortalEnemySpawnPoint(new PointI(50, 50), false, 5));
        assertEquals(new PointD(50, 48), map.getPortalEnemySpawnPoint(new PointI(50, 50), false, 6));
    }
}
