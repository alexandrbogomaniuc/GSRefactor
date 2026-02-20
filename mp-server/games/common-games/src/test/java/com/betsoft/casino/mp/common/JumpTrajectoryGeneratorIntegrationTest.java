package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.GameType;
import org.junit.Before;
import org.junit.Test;
import org.kynosarges.tektosyne.geometry.PointI;

public class JumpTrajectoryGeneratorIntegrationTest {

    private GameMapStore gameMapStore;

    @Before
    public void setUp() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test
    public void shouldGenerateTrajectoriesFromAllSpawnPoints() {
        for (int mapId : GameType.AMAZON.getMaps()) {
            GameMapShape map = gameMapStore.getMap(mapId);
            for (PointI spawnPoint : map.getSpawnPoints()) {
                for (int i = 0; i < 10000; i++) {
                    new JumpTrajectoryGenerator(map, spawnPoint, 5.0f, 19, 29)
                            .generateWithDuration(System.currentTimeMillis() + 1000, 30000, true);
                }
            }
        }
    }
}
