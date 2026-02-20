package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.kynosarges.tektosyne.geometry.PointI;

import static org.junit.Assert.assertTrue;

public class MinStepTrajectoryGeneratorIntegrationTest {

    private static final Logger LOG = LogManager.getLogger(MinStepTrajectoryGeneratorIntegrationTest.class);

    private GameMapStore gameMapStore;

    @Before
    public void setUp() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test
    public void checkThatTrajectoryCouldBeGeneratedFromAllPoints() {
        for (int mapId : GameType.REVENGE_OF_RA.getMaps()) {
            GameMapShape map = gameMapStore.getMap(mapId);
            for (PointI spawn : map.getSpawnPoints()) {
                for (int i = 0; i < 1000; i++) {
                    Trajectory trajectory = null;
                    try {
                        trajectory = new MinStepTrajectoryGenerator(map, spawn, 1, 17, 22).generate(0, 5);
                        if (!trajectory.getPoints().isEmpty()) {
                            Point last = trajectory.getPoints().get(trajectory.getPoints().size() - 1);
                            assertTrue(map.isSpawnPoint((int) last.getX(), (int) last.getY()));
                        }
                    } catch (BadTrajectoryException e) {
                        // ignored
                    } catch (Throwable e) {
                        LOG.error("{}, {}", mapId, trajectory, e);
                    }
                }
            }
        }
    }
}
