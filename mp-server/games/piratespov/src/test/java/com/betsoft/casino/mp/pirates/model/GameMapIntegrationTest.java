package com.betsoft.casino.mp.pirates.model;

import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.piratespov.model.GameMap;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class GameMapIntegrationTest {

    private GameMapStore gameMapStore;

    @Before
    public void setUp() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test
    public void trollTrajectoryShouldNotBeEmptyAndShouldNotContainDuplicatedPoints() {
        for (int mapId : GameType.PIRATES_POV.getMaps()) {
            GameMap map = new GameMap(null, gameMapStore.getMap(mapId));
            for (int i = 0; i < 1000; i++) {
                Trajectory trajectory = map.generateRandomTrollTrajectory(8.0f);
                assertTrue(trajectory.isEmpty() || !hasDuplicatePoints(trajectory.getPoints()));
            }
        }
    }

    private boolean hasDuplicatePoints(List<Point> points) {
        for (int i = 1; i < points.size(); i++) {
            Point a = points.get(i - 1);
            Point b = points.get(i);
            if (a.getX() == b.getX() && a.getY() == b.getY()) {
                return true;
            }
        }
        return false;
    }
}
