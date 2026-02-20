package com.betsoft.casino.mp.sectorx.model.math.config;

import com.betsoft.casino.mp.sectorx.model.math.BossType;
import com.betsoft.casino.mp.sectorx.model.math.EnemyType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class GameConfigLoaderTest {

    public static final double DOUBLE_DELTA = 0.001;
    @InjectMocks
    private GameConfigLoader loader;

    @Test
    public void loadDefaultConfig() {
        // if you are running this test in your IDE make sure to clean and build the common-games
        // cd games/common-games/
        // mvn clean install/compile

        GameConfig gameConfig = loader.loadDefaultConfig();
        assertEquals("SXSP: V24", gameConfig.getMathVersion());
        assertEquals(93.0d, gameConfig.getGameRTP(), DOUBLE_DELTA);

        assertEquals(60, gameConfig.getEnemies().get(EnemyType.B1).stream().findFirst().get().getMinPay());
        assertEquals(300, gameConfig.getEnemies().get(EnemyType.B1).stream().findFirst().get().getMaxPay());

        assertEquals(50, gameConfig.getEnemies().get(EnemyType.B2).stream().findFirst().get().getMinPay());
        assertEquals(200, gameConfig.getEnemies().get(EnemyType.B2).stream().findFirst().get().getMaxPay());

        assertEquals(45, gameConfig.getEnemies().get(EnemyType.B3).stream().findFirst().get().getMinPay());
        assertEquals(150, gameConfig.getEnemies().get(EnemyType.B3).stream().findFirst().get().getMaxPay());

        assertEquals(20, gameConfig.getItems().get(EnemyType.F1).stream().findFirst().get().getPrizes().get(0).getPay());
        assertEquals(100, gameConfig.getItems().get(EnemyType.F4).stream().findFirst().get().getPrizes().get(0).getMaxPay());

        BossParams bossParams = gameConfig.getBosses().get(BossType.BOSS_1.getSkinId());

        assertEquals(1, bossParams.getMinPay());
        assertEquals(70, bossParams.getMaxPay());
        assertEquals(60, bossParams.getFixedPay());

        assertEquals(22, bossParams.getMaxHP());
        assertEquals(0.45, bossParams.getMinRTPFixedPay(), DOUBLE_DELTA);
        assertEquals(1.15, bossParams.getMaxRTPFixedPay(), DOUBLE_DELTA);

        assertEquals(0.925, bossParams.getMinRTPSmallPay(), DOUBLE_DELTA);
        assertEquals(0.93, bossParams.getMaxRTPSmallPay(), DOUBLE_DELTA);

        assertEquals(0.45, bossParams.getMinRTPBigPay(), DOUBLE_DELTA);
        assertEquals(1.1, bossParams.getMaxRTPBigPay(), DOUBLE_DELTA);

    }
}
