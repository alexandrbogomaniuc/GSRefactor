package com.betsoft.casino.mp.sectorx.model;

import com.betsoft.casino.mp.common.ShootResult;
import com.betsoft.casino.mp.common.testmodel.StubRoomPlayerInfo;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.sectorx.model.math.EnemyData;
import com.betsoft.casino.mp.sectorx.model.math.EnemyType;
import com.betsoft.casino.mp.sectorx.model.math.config.*;
import com.betsoft.casino.mp.service.*;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.RNG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EnemyGameTest {

    @Mock
    private Logger logger = LogManager.getLogger(EnemyGameTest.class);
    @Mock
    private IGameConfigService gameConfigService;

    @Mock
    private IGameConfigProvider gameConfigProvider;

    @Mock
    private SpawnConfigProvider spawnConfigProvider;

    @InjectMocks
    @Spy
    private EnemyGame enemyGame;

    @Mock
    private EnemySpecialItem enemy;

    @Mock
    private BossParams bossParams;

    @Mock
    private ITransportObjectsFactoryService toService;

    public static final double HUGE_ENEMY_RTP_START = 0.92;
    public static final double HUGE_ENEMY_RTP_END = 0.98;
    public static final double SPECIAL_ITEM_RTP_START = 0.90;
    public static final double SPECIAL_ITEM_RTP_END = 0.98;

    @Test
    public void test_payout_for_low_pay_enemies() throws CommonException {
        try (MockedStatic<RNG> rng = Mockito.mockStatic(RNG.class)) {
            rng.when(RNG::rand).thenReturn(0.38);

            EnemyType[] typesToTest = {
                    EnemyType.S1, EnemyType.S2, EnemyType.S3, EnemyType.S4,
                    EnemyType.S5, EnemyType.S6, EnemyType.S7, EnemyType.S8,
                    EnemyType.S9, EnemyType.S10, EnemyType.S11, EnemyType.S12,
                    EnemyType.S13
            };

            for (EnemyType type : typesToTest) {
                assertEquals(Money.fromCents(4), testShootResult(type).getWin());
            }
        }
    }

    @Test
    public void test_payout_for_mid_pay_enemies() throws CommonException {
        try (MockedStatic<RNG> rng = Mockito.mockStatic(RNG.class)) {
            rng.when(RNG::rand).thenReturn(0.23);

            EnemyType[] typesToTest = {
                    EnemyType.S14, EnemyType.S15, EnemyType.S16, EnemyType.S17,
                    EnemyType.S18, EnemyType.S19, EnemyType.S20, EnemyType.S21,
                    EnemyType.S22, EnemyType.S23, EnemyType.S24, EnemyType.S25,
                    EnemyType.S26
            };

            for (EnemyType type : typesToTest) {
                assertEquals(Money.fromCents(8), testShootResult(type).getWin());
            }
        }
    }

    @Test
    public void test_payout_for_high_pay_enemies() throws CommonException {
        try (MockedStatic<RNG> rng = Mockito.mockStatic(RNG.class)) {
            rng.when(RNG::rand).thenReturn(0.15);

            EnemyType[] typesToTest = {
                    EnemyType.S27, EnemyType.S28, EnemyType.S29,
                    EnemyType.S30, EnemyType.S31
            };

            for (EnemyType type : typesToTest) {
                assertEquals(Money.fromCents(12), testShootResult(type).getWin());
            }
        }
    }

    @Test
    public void test_payout_for_huge_pay_enemies() throws CommonException {
        EnemyType[] typesToTest = {
                EnemyType.B1, EnemyType.B2, EnemyType.B3
        };
        try (MockedStatic<RNG> rng = Mockito.mockStatic(RNG.class)) {
            for (EnemyType type : typesToTest) {
                rng.when(RNG::rand).thenReturn(0.002);
                rng.when(() -> RNG.nextInt(2, 5)).thenReturn(3);
                rng.when(() -> RNG.randExponential(HUGE_ENEMY_RTP_START, HUGE_ENEMY_RTP_END)).thenReturn(0.99);

                assertEquals(Money.fromCents(6), testShootResult(type).getWin());
            }
        }
    }

    @Ignore
    @Test
    public void test_payout_for_special_items_f1_f7() throws CommonException {
        try (MockedStatic<RNG> rng = Mockito.mockStatic(RNG.class)) {
            rng.when(RNG::rand).thenReturn(0.013);
            rng.when(() -> RNG.randExponential(SPECIAL_ITEM_RTP_START, SPECIAL_ITEM_RTP_END)).thenReturn(0.99);
            when(enemy.getTotalPayout()).thenReturn(3);

            EnemyType[] typesToTest = {
                    EnemyType.F1, EnemyType.F7
            };

            for (EnemyType type : typesToTest) {
                assertEquals(Money.fromCents(6), testShootResult(type).getWin());
            }
        }
    }

    @Ignore
    @Test
    public void test_payout_for_special_items_f4() throws CommonException {
        try (MockedStatic<RNG> rng = Mockito.mockStatic(RNG.class)) {
            rng.when(RNG::rand).thenReturn(0.07);
            rng.when(() -> RNG.nextIntUniform(2, 3)).thenReturn(2);
            rng.when(() -> RNG.randExponential(SPECIAL_ITEM_RTP_START, SPECIAL_ITEM_RTP_END)).thenReturn(0.99);
            when(enemy.getTotalPayout()).thenReturn(2);

            assertEquals(Money.fromCents(4), testShootResult(EnemyType.F4).getWin());
        }
    }

    @Ignore
    @Test
    public void test_payout_for_special_items() throws CommonException {
        try (MockedStatic<RNG> rng = Mockito.mockStatic(RNG.class)) {
            rng.when(RNG::rand).thenReturn(0.013);
            rng.when(() -> RNG.nextIntUniform(3, 5)).thenReturn(2);
            rng.when(() -> RNG.randExponential(SPECIAL_ITEM_RTP_START, SPECIAL_ITEM_RTP_END)).thenReturn(0.99);
            when(enemy.getTotalPayout()).thenReturn(2);

            EnemyType[] typesToTest = {
                    EnemyType.F2, EnemyType.F3, EnemyType.F5, EnemyType.F6
            };

            for (EnemyType type : typesToTest) {
                assertEquals(Money.fromCents(4), testShootResult(type).getWin());
            }
        }
    }

    @Test
    public void test_payout_for_boss_rage_mode() throws CommonException {
        try (MockedStatic<RNG> rng = Mockito.mockStatic(RNG.class)) {
            rng.when(RNG::rand).thenReturn(0.02);
            rng.when(RNG::randUniform).thenReturn(0.5);

            when(enemy.getFullEnergy()).thenReturn(100.0);
            when(enemy.getEnergy()).thenReturn(6.0);

            rng.when(() -> RNG.randExponential(0.1, 0.4)).thenReturn(0.3);

            assertEquals(Money.fromCents(20), testShootResult(EnemyType.BOSS).getWin());
            verify(enemy).setEnergy(1);
        }
    }

    @Test
    public void test_payout_for_boss_partial_pay_lower_or_equal_than_low_pay_threshold() throws CommonException {
        try (MockedStatic<RNG> rng = Mockito.mockStatic(RNG.class)) {
            rng.when(RNG::rand).thenReturn(0.023);
            rng.when(RNG::randUniform).thenReturn(0.5);

            when(enemy.getFullEnergy()).thenReturn(100.0);
            when(enemy.getEnergy()).thenReturn(11.0);

            when(bossParams.getLowPayThreshold()).thenReturn(13);
            when(bossParams.getMinRTPSmallPay()).thenReturn(0.1);
            when(bossParams.getMaxRTPSmallPay()).thenReturn(0.4);

            rng.when(() -> RNG.randExponential(0.1, 0.4)).thenReturn(0.3);

            assertEquals(Money.fromCents(11), testShootResult(EnemyType.BOSS).getWin());

            verify(enemy).setEnergy(8.1875);
        }
    }

    @Test
    public void test_payout_for_boss_partial_pay_higher_than_low_pay_threshold() throws CommonException {
        try (MockedStatic<RNG> rng = Mockito.mockStatic(RNG.class)) {
            rng.when(RNG::rand).thenReturn(0.023);
            rng.when(RNG::randUniform).thenReturn(0.5);

            when(enemy.getFullEnergy()).thenReturn(100.0);
            when(enemy.getEnergy()).thenReturn(11.0);

            when(bossParams.getLowPayThreshold()).thenReturn(4);
            when(bossParams.getMinRTPBigPay()).thenReturn(0.1);
            when(bossParams.getMaxRTPBigPay()).thenReturn(0.4);

            rng.when(() -> RNG.randExponential(0.1, 0.4)).thenReturn(0.3);

            assertEquals(Money.fromCents(11), testShootResult(EnemyType.BOSS).getWin());
            verify(enemy).setEnergy(8.1875);
        }

    }

    private ShootResult testShootResult(EnemyType enemyType) throws CommonException {
        int stakeCents = 2;
        long roomId = 1L;

        // Using EnemySpecialItem instead of Enemy because EnemySpecialItem is a subclass of Enemy
        when(enemy.getEnemyType()).thenReturn(enemyType);
        when(enemy.isBoss()).thenReturn(enemyType.isBoss());

        when(enemy.getSkin()).thenReturn(1);
        when(enemy.getCurrentMultiplier()).thenReturn(3);
        when(enemy.getEnemyClass()).thenReturn(new EnemyClass(enemyType.getId(), enemyType.getName(), 1, 100, enemyType));

        EnemyData enemyData = mock(EnemyData.class);
        when(enemyData.getPay()).thenReturn(2);
        when(enemyData.getMinPay()).thenReturn(2);
        when(enemyData.getMaxPay()).thenReturn(4);

        Map<Integer, BossParams> skinToBossMap = new HashMap<>();
        when(bossParams.getFixedPay()).thenReturn(10);
        when(bossParams.getMinRTPFixedPay()).thenReturn(0.1);
        when(bossParams.getMaxRTPFixedPay()).thenReturn(0.4);
        when(bossParams.getMinPay()).thenReturn(5);
        when(bossParams.getMaxPay()).thenReturn(10);
        when(bossParams.getDamageDivider()).thenReturn(2d);

        skinToBossMap.put(1, bossParams);

        Map<CriticalMultiplierType, List<Double>> criticalMultiplierTypeListHashMap = new HashMap<>();
        criticalMultiplierTypeListHashMap.put(CriticalMultiplierType.LP, Arrays.asList(1.0, 0.0));
        criticalMultiplierTypeListHashMap.put(CriticalMultiplierType.MP, Arrays.asList(0.0, 1.0, 0.0));
        criticalMultiplierTypeListHashMap.put(CriticalMultiplierType.HP, Arrays.asList(0.0, 0.0, 1.0));

        GameConfig gameConfig = mock(GameConfig.class);
        when(gameConfig.getGameRTP()).thenReturn(97.5);
        when(gameConfig.getEnemyData(enemyType)).thenReturn(enemyData);
        when(gameConfig.getBosses()).thenReturn(skinToBossMap);
        when(gameConfig.getCriticalMultiplier()).thenReturn(criticalMultiplierTypeListHashMap);

        when(gameConfigProvider.getConfig(GameType.SECTOR_X.getGameId(), roomId)).thenReturn(gameConfig);

        IShot shot = mock(IShot.class);

        Seat seat = mock(Seat.class);
        when(seat.getBetLevel()).thenReturn(1);
        when(seat.getActualShot()).thenReturn(shot);

        StubRoomPlayerInfo playerInfo = mock(StubRoomPlayerInfo.class);
        when(playerInfo.getRoomId()).thenReturn(roomId);
        IPlayerStats playerStats = mock(IPlayerStats.class);

        when(playerInfo.getRoundStats()).thenReturn(playerStats);
        when(seat.getPlayerInfo()).thenReturn(playerInfo);

        PlayerRoundInfo playerRoundInfo = mock(PlayerRoundInfo.class);
        when(seat.getCurrentPlayerRoundInfo()).thenReturn(playerRoundInfo);

        when(gameConfigProvider.getConfig(GameType.SECTOR_X.getGameId(), roomId)).thenReturn(gameConfig);

        Money stake = Money.fromCents(stakeCents);

        boolean isMainShot = true;

        return enemyGame.doShoot(enemy, seat, stake, enemyType == EnemyType.BOSS, toService, isMainShot, 0, 1);
    }
}
