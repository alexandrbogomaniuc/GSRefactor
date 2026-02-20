package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.GameType;
import org.junit.Before;
import org.junit.Test;

import static com.betsoft.casino.mp.model.GameType.PIRATES_POV;
import static org.junit.Assert.assertTrue;

public class MapValidationTest {

    private GameMapStore gameMapStore;

    @Before
    public void setup() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test
    public void mapShouldNotContainCellsMarkedWithMultipleTypes() {
        for (GameType gameType : GameType.values()) {
            if (PIRATES_POV != gameType && !gameType.isBattleGroundGame()) {
                for (int mapId : gameType.getMaps()) {
                    GameMapShape map = gameMapStore.getMap(mapId);
                    assertTrue(map.validate());
                }
            }
        }
    }
}
