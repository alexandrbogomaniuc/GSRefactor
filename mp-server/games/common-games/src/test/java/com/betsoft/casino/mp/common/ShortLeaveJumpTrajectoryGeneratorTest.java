package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.GameType;
import org.junit.Before;
import org.junit.Test;
import org.kynosarges.tektosyne.geometry.PointI;

import static org.junit.Assert.*;

public class ShortLeaveJumpTrajectoryGeneratorTest {

    private GameMapStore gameMapStore;

    @Before
    public void setUp() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test(timeout = 10000L)
    public void trajectoryCouldBeGeneratedFromAllPoints() {
        for (int mapId : GameType.MISSION_AMAZON.getMaps()) {
            GameMapShape map = gameMapStore.getMap(mapId);
            for (int x = 0; x < map.getWidth(); x++) {
                for (int y = 0; y < map.getHeight(); y++) {
                    if (map.isPassable(x, y)) {
                        for (int dir = 0; dir < 4; dir++) {
                            assertTrue(new ShortLeaveJumpTrajectoryGenerator(map, new PointI(x, y), 1.0, 3, 7, dir)
                                    .generate(1000)
                                    .getPoints()
                                    .size() < 30);
                        }
                    }
                }
            }
        }
    }
}
