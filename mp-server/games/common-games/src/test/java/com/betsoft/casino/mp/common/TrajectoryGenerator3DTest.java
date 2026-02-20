package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.movement.generators.TrajectoryGenerator3D;
import org.junit.Before;
import org.junit.Test;
import org.kynosarges.tektosyne.geometry.PointI;

public class TrajectoryGenerator3DTest {

    private GameMapStore gameMapStore;

    @Before
    public void setUp() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test
    public void canFindExitFromAllPoints() {
        for (int mapId : GameType.PIRATES_POV.getMaps()) {
            GameMapShape map = gameMapStore.getMap(mapId);
            for (PointI spawnPoint : map.getSpawnPoints()) {
                new TrajectoryGenerator3D(map, spawnPoint, 1)
                        .generate(1000L, 10, true);
            }
        }
    }
}
