package com.betsoft.casino.mp.piratesdmc.model;

import com.betsoft.casino.mp.common.GameMapShape;
import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.common.WaypointTrajectoryGenerator;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class WaypointTrajectoryGeneratorTest {

    private GameMapStore gameMapStore;

    @Before
    public void setUp() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test
    public void checkThatAllExitTrajectoriesCouldBeFound() {
        long date = System.currentTimeMillis();
        for (int mapId : GameType.DMC_PIRATES.getMaps()) {
            GameMapShape map = gameMapStore.getMap(mapId);
            WaypointTrajectoryGenerator generator = new WaypointTrajectoryGenerator(map);
            for (int x = 0; x < map.getWidth(); x++) {
                for (int y = 0; y < map.getHeight(); y++) {
                    Trajectory trajectory = generator.generate(x, y, date, 0.01);
                    Point last = trajectory.getPoints().get(trajectory.getPoints().size() - 1);
                    int lastX = (int) last.getX();
                    int lastY = (int) last.getY();
                    assertTrue(!map.isValid(lastX, lastY) || map.isSpawnPoint(lastX, lastY));
                }
            }
        }
    }
}