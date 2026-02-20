package com.betsoft.casino.mp.movement.generators;

import com.betsoft.casino.mp.common.GameMapShape;
import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.model.movement.Point;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class RushTrajectoryGenerator3DTest {

    private GameMapStore gameMapStore;

    @Before
    public void setUp() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test
    public void generate() {
        GameMapShape map = gameMapStore.getMap(401);
        List<Point> points = new RushTrajectoryGenerator3D(map, map.getSpawnPoints().get(0), 4.0f, 8.0f)
                .generate(1000L, 30, false).getPoints();
        for (int i = 1; i < points.size(); i++) {
            assertTrue(points.get(i - 1).getTime() < points.get(i).getTime());
        }
    }
}
