package com.betsoft.casino.mp.clashofthegods.model;

import com.betsoft.casino.mp.common.GameMapShape;
import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.common.SwarmParams;
import com.betsoft.casino.mp.common.WaypointTrajectoryGenerator;
import com.betsoft.casino.mp.common.scenarios.SpawnGroup;
import com.betsoft.casino.mp.common.scenarios.SpawnScenario;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.movement.*;
import com.betsoft.casino.mp.clashofthegods.model.math.EnemyRange;
import com.betsoft.casino.mp.clashofthegods.model.math.EnemyType;
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
        for (int mapId : GameType.CLASH_OF_THE_GODS.getMaps()) {
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
        for (int mapId : GameType.CLASH_OF_THE_GODS.getMaps()) {
            GameMapShape map = gameMapStore.getMap(mapId);
            for (SwarmParams params : map.getSwarmParams()) {
                if (params.isFromPortal()) {
                    String message = "Map: " + mapId + ", id: " + params.getId();
                    assertTrue(message, params.getAngle() > 190 && params.getAngle() < 350);
                }
            }
        }
    }

}
