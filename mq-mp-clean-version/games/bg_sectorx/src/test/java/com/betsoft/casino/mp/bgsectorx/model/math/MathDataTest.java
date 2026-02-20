package com.betsoft.casino.mp.bgsectorx.model.math;

import com.betsoft.casino.mp.bgsectorx.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.bgsectorx.model.math.config.SpawnConfigLoader;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

public class MathDataTest {

    @Test
    public void testGameConfigLoaderInitialized() {
        assertNotNull(new GameConfigLoader().loadDefaultConfig());
    }

    @Test
    public void testSpawnConfigLoaderInitialized() {
        assertNotNull(new SpawnConfigLoader().loadDefaultConfig());
    }

    @Test
    public void testPayTableCouldBeInstantiated() {
        assertNotNull(PayTableInst.getTable());
    }
}
