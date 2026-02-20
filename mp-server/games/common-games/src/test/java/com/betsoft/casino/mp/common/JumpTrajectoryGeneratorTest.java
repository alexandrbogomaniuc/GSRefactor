package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.movement.Point;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.List;

import static org.junit.Assert.*;

public class JumpTrajectoryGeneratorTest {
    private static final Logger LOG = LogManager.getLogger(JumpTrajectoryGeneratorTest.class);

    private GameMapStore gameMapStore;

    @Before
    public void setUp() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test
    public void shouldUseCorrectSpeedOnAllSegments() {
        for (int mapId : GameType.AMAZON.getMaps()) {
            GameMapShape map = gameMapStore.getMap(mapId);
            for (PointI spawnPoint : map.getSpawnPoints()) {
                List<Point> points = new JumpTrajectoryGenerator(map, spawnPoint, 5.0f, 9, 12)
                        .generate(System.currentTimeMillis() + 1000, 7, true)
                        .getPoints();
                for (int i = 1; i < points.size(); i++) {
                    try {
                        assertTrue(calculateSpeed(points.get(i - 1), points.get(i)) >= 5.0);
                    } catch (Throwable e) {
                        LOG.error("Too low speed at segment {} in {}", i, points);
                        throw e;
                    }
                }
            }
        }
    }

    private double calculateSpeed(Point a, Point b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        long dt = b.getTime() - a.getTime();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance / dt * 1000.0;
    }
}
