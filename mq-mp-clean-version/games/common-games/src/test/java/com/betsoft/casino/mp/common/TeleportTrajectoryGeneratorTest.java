package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.GameType;
import com.dgphoenix.casino.common.util.RNG;
import org.junit.Before;
import org.junit.Test;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.List;

public class TeleportTrajectoryGeneratorTest {

    private GameMapStore gameMapStore;

    @Before
    public void setUp() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test
    public void testGenerateWithDuration() {
        GameMapShape map = getMap();
        TeleportTrajectoryGenerator generator = new TeleportTrajectoryGenerator(map, getRandomSpawnPoint(map), 4.0f, 2000, 2000, 7000, 1, true);
        generator.generate(100L, 15, true);
    }

    @Test
    public void testGenerate() {
        GameMapShape map = getMap();
        TeleportTrajectoryGenerator generator = new TeleportTrajectoryGenerator(map, getRandomSpawnPoint(map), 4.0f, 2000, 2000, 7000, 1, true);
        generator.generate(100L, 15, true);
    }

    private GameMapShape getMap() {
        return gameMapStore.getMap(GameType.AMAZON.getMaps().get(0));
    }

    private PointI getRandomSpawnPoint(GameMapShape map) {
        List<PointI> spawns = map.getSpawnPoints();
        return spawns.get(RNG.nextInt(spawns.size()));
    }

}
