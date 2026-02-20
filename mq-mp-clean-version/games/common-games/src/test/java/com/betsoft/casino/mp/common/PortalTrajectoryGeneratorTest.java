package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.GameType;
import org.junit.Before;
import org.junit.Test;

public class PortalTrajectoryGeneratorTest {

    private GameMapStore gameMapStore;

    @Before
    public void setUp() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test
    public void canGenerateTrajectories() {
        for (int mapId : GameType.REVENGE_OF_RA.getMaps()) {
            GameMapShape map = gameMapStore.getMap(mapId);
            new PortalTrajectoryGenerator(map, new Coords(960, 540, 96, 96), 4.0f, 1)
                    .generate(100L, 100, true);
        }
    }
}
