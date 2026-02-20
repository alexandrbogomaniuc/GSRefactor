package com.betsoft.casino.mp.dragonstone.model.math;

import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfig;
import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfigLoader;
import org.junit.Test;

import static com.betsoft.casino.mp.model.SpecialWeaponType.*;
import static org.junit.Assert.assertEquals;

public class MathDataTest {

    @Test
    public void testWeaponsRTP() {
        //assertEquals(3.59375, MathData.calculateAveragePayWithoutSlotAndWeapons(18, 4.8), 0.0000001);
    }

    @Test
    public void testWeaponsProbKillBossRTP() {
        GameConfig config = new GameConfigLoader().loadDefaultConfig();
        assertEquals(0.0024, MathData.getHitKillProbabilityForBoss(config, -1), 0.001);
        assertEquals(0.0034, MathData.getHitKillProbabilityForBoss(config, Flamethrower.getId()), 0.001);
    }
}
