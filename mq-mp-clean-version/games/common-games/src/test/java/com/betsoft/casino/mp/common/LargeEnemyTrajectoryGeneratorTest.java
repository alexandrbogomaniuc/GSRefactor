package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.GameType;
import org.junit.Before;
import org.junit.Test;
import org.kynosarges.tektosyne.geometry.PointI;

public class LargeEnemyTrajectoryGeneratorTest {

    private GameMapStore gameMapStore;

    @Before
    public void setUp() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test
    public void shouldGenerateTrajectoriesForAllSpawnPoints() {
        for (int mapId : GameType.AMAZON.getMaps()) {
            GameMapShape map = gameMapStore.getMap(mapId);
            for (PointI source : map.getLargeEnemiesSpawnPoints()) {
                new LargeEnemyFreeAngleTrajectoryGenerator(map, source, 5.0f)
                        .generateWithDuration(System.currentTimeMillis() + 1000, 30000, true);
            }
        }
    }

}
