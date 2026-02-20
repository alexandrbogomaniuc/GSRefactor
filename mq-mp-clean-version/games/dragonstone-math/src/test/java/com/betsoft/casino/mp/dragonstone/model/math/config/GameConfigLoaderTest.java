package com.betsoft.casino.mp.dragonstone.model.math.config;

import org.junit.Test;

public class GameConfigLoaderTest {

    @Test
    public void testLoadDefaultConfig() {
        new GameConfigLoader().loadDefaultConfig();
    }
}
