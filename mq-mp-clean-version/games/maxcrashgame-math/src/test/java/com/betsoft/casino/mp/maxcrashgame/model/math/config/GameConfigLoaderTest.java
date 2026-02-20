package com.betsoft.casino.mp.maxcrashgame.model.math.config;

import org.junit.Test;

public class GameConfigLoaderTest {

    @Test
    public void testLoadDefaultConfig() {
        new GameConfigLoader().loadDefaultConfig();
    }
}
