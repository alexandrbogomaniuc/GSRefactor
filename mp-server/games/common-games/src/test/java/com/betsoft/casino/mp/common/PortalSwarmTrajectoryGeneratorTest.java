package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.abs.casino.common.util.RNG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.List;

import static org.junit.Assert.*;

public class PortalSwarmTrajectoryGeneratorTest {
    private static final Logger LOG = LogManager.getLogger(PortalSwarmTrajectoryGeneratorTest.class);

    private GameMapStore gameMapStore;

    @Before
    public void setUp() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test
    public void generate() {
        Coords coords = new Coords(960, 540, 96, 96);
        for (int mapId : GameType.REVENGE_OF_RA.getMaps()) {
            GameMapShape map = gameMapStore.getMap(mapId);
            for (SwarmParams params : map.getSwarmParams()) {
                RatSwarmTrajectoryGenerator generator = params.isFromPortal()
                        ? new PortalSwarmTrajectoryGenerator(map, coords, params.getAngle(), 1)
                        : new RatSwarmTrajectoryGenerator(map, coords, params.getAngle());

                Trajectory trajectory = generator
                        .generate(new PointD(params.getStartX() + RNG.nextInt(params.getDeltaX()), params.getStartY() + RNG.nextInt(params.getDeltaY())),
                                params.getDistance(), 6.0f, 1.0f, System.currentTimeMillis(), 500);
                List<Point> points = trajectory.getPoints();

                for (int i = 1; i < points.size(); i++) {
                    try {
                        assertTrue(points.get(i - 1).getTime() < points.get(i).getTime());
                    } catch (Throwable e) {
                        LOG.error("mapId: {}, params: {}, trajectory: {}", mapId, params, trajectory);
                        throw e;
                    }
                }
            }
        }
    }
}
