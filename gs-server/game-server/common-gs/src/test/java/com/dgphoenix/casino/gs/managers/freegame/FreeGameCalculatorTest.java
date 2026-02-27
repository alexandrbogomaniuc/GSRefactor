package com.abs.casino.gs.managers.freegame;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.bank.Limit;
import com.dgphoenix.casino.common.cache.data.game.BaseGameConstants;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfo;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.game.GameGroup;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.dblink.FreeGameCalculator;
import com.dgphoenix.casino.gs.managers.game.settings.GameSettingsManager;
import com.dgphoenix.casino.gs.managers.payment.currency.CurrencyRatesManager;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static com.dgphoenix.casino.common.cache.data.game.BaseGameConstants.KEY_FREEBALANCE;
import static com.dgphoenix.casino.common.cache.data.game.BaseGameConstants.KEY_FREEBALANCE_MULTIPLIER;
import static com.dgphoenix.casino.common.config.GameServerConfigTemplate.DEFAULT_FREE_BALANCE;
import static com.dgphoenix.casino.gs.managers.dblink.FreeGameCalculator.DEFAULT_MQ_STAKES_RESERVE;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FreeGameCalculatorTest {
    @Mock
    private GameServerConfiguration serverConfig;
    @Mock
    private CurrencyRatesManager currencyConverter;
    @Mock
    private GameSettingsManager gameSettingsManager;
    @Mock
    private BankInfoCache bankInfoCache;
    @Mock
    private BaseGameInfoTemplateCache gameInfoTemplateCache;
    @Mock
    private BaseGameInfoTemplate baseGameInfoTemplate;
    @Mock
    private BaseGameInfo gameInfo;
    @Spy
    private BankInfo bankInfo;

    private FreeGameCalculator freeGameCalculator;

    @Before
    public void setUp() {
        when(bankInfoCache.getBankInfo(anyLong())).thenReturn(this.bankInfo);
        freeGameCalculator = new FreeGameCalculator(serverConfig,
                currencyConverter, gameSettingsManager, bankInfoCache, gameInfoTemplateCache);
    }

    @Test
    public void freeBalanceInGameServerConfigOnly() {
        long serverFreeBalanceProperty = 10L;
        when(gameInfo.getProperty(any())).thenReturn(null);
        when(serverConfig.getFreeBalanceMultiplier()).thenReturn(null);
        when(serverConfig.getFreeBalance()).thenReturn(serverFreeBalanceProperty);

        long freeBalance = freeGameCalculator.calculateFreeBalance(gameInfo, "EUR");

        assertEquals("Should equals with GameServerConfig property value", serverFreeBalanceProperty, freeBalance);
    }

    @Test
    public void dropFreeBalance() {
        long serverFreeBalanceProperty = 100;
        mockDropFreeBalance();
        when(serverConfig.getFreeBalance()).thenReturn(serverFreeBalanceProperty);
        when(gameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong())).thenReturn(baseGameInfoTemplate);

        long freeBalance = freeGameCalculator.calculateFreeBalance(gameInfo, "EUR");

        assertEquals("Should equals with GameServerConfig property value. GL not supported", serverFreeBalanceProperty, freeBalance);
    }

    @Test
    public void dropFreeBalanceWithGLSupported() throws CommonException {
        double convertedFreeBalance = 100001;
        mockDropFreeBalance();
        when(serverConfig.getFreeBalance()).thenReturn(777L);
        when(gameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong())).thenReturn(baseGameInfoTemplate);
        when(baseGameInfoTemplate.isDynamicLevelsSupported()).thenReturn(true);
        when(currencyConverter.convert(anyDouble(), anyString(), anyString())).thenReturn(convertedFreeBalance);

        long freeBalance = freeGameCalculator.calculateFreeBalance(gameInfo, "EUR");

        assertEquals("Should equals with GameServerConfig property value. Drop balance and GL supported",
                (long) convertedFreeBalance, freeBalance);
    }

    @Test
    public void dropFreeBalanceWithGLSupportedWhenLessThanDefault() throws CommonException {
        double convertedFreeBalance = 80000;
        mockDropFreeBalance();
        when(serverConfig.getFreeBalance()).thenReturn(777L);
        when(gameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong())).thenReturn(baseGameInfoTemplate);
        when(baseGameInfoTemplate.isDynamicLevelsSupported()).thenReturn(true);
        when(currencyConverter.convert(anyDouble(), anyString(), anyString())).thenReturn(convertedFreeBalance);

        long freeBalance = freeGameCalculator.calculateFreeBalance(gameInfo, "EUR");

        assertEquals("Free balance should not be less than default after conversion", DEFAULT_FREE_BALANCE, freeBalance);
    }

    @Test
    public void dropFreeBalanceWithGLSupportedWhenConverterThrowException() throws CommonException {
        long serverFreeBalanceProperty = 100;
        mockDropFreeBalance();
        when(serverConfig.getFreeBalance()).thenReturn(serverFreeBalanceProperty);
        when(gameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong())).thenReturn(baseGameInfoTemplate);
        when(baseGameInfoTemplate.isDynamicLevelsSupported()).thenReturn(true);
        when(currencyConverter.convert(anyDouble(), anyString(), anyString())).thenThrow(CommonException.class);

        long freeBalance = freeGameCalculator.calculateFreeBalance(gameInfo, "EUR");

        assertEquals("Should equals with GameServerConfig freeBalance property value if converter throwing exception",
                serverFreeBalanceProperty, freeBalance);
    }

    @Test
    public void freeBalanceMultiplierInGameServerConfigWithTableGame() {
        int serverFBMpProperty = 2;
        when(gameInfo.getProperty(any())).thenReturn(null);
        when(serverConfig.getFreeBalanceMultiplier()).thenReturn(serverFBMpProperty);
        when(gameInfo.getGroup()).thenReturn(GameGroup.TABLE);
        when(gameInfo.getLimit()).thenReturn(Limit.getByValue(2, 25));

        long freeBalance = freeGameCalculator.calculateFreeBalance(gameInfo, "EUR");

        assertEquals("Should equals rounded product of server freeBalanceMultiplier and game max limit",
                freeGameCalculator.getRoundBalance(25 * serverFBMpProperty), freeBalance);
    }

    @Test
    public void freeBalanceMultiplierInGameServerConfigWithMQGameWhenMqStakesReserveMissing() {
        int serverFBMpProperty = 2;
        int maxCoinValue = 50;
        List<Coin> coins = Collections.singletonList(Coin.getByValue(maxCoinValue));
        when(serverConfig.getFreeBalanceMultiplier()).thenReturn(serverFBMpProperty);
        when(gameInfo.getGroup()).thenReturn(GameGroup.ACTION_GAMES);
        mockConverterData(coins);

        long freeBalance = freeGameCalculator.calculateFreeBalance(gameInfo, "EUR");

        assertEquals("Should equals rounded product of server freeBalanceMultiplier, max coins and default stakes reserve values",
                freeGameCalculator.getRoundBalance(maxCoinValue * DEFAULT_MQ_STAKES_RESERVE * serverFBMpProperty), freeBalance);
    }

    @Test
    public void freeBalanceMultiplierInGameServerConfigWithMQGameWhenMqStakesReserveExist() {
        int serverFBMpProperty = 2;
        int maxCoinValue = 50;
        List<Coin> coins = Collections.singletonList(Coin.getByValue(maxCoinValue));
        String mqStakesReserveGameProperty = "180";
        when(serverConfig.getFreeBalanceMultiplier()).thenReturn(serverFBMpProperty);
        when(gameInfo.getGroup()).thenReturn(GameGroup.ACTION_GAMES);
        mockConverterData(coins);
        when(gameInfo.getProperty(BaseGameConstants.KEY_MQ_STAKES_RESERVE)).thenReturn(mqStakesReserveGameProperty);

        long freeBalance = freeGameCalculator.calculateFreeBalance(gameInfo, "EUR");

        assertEquals("Should equals rounded product of server freeBalanceMultiplier, max coins and present stakes reserve values",
                freeGameCalculator.getRoundBalance(maxCoinValue * Long.parseLong(mqStakesReserveGameProperty) * serverFBMpProperty), freeBalance);
    }

    @Test
    public void freeBalanceMultiplierInGameServerConfigWithVideoPokerGameWhenMaxBetInCreditMissing() {
        List<Coin> coins = Collections.singletonList(Coin.getByValue(50));
        long serverFreeBalanceProperty = 10L;
        when(serverConfig.getFreeBalanceMultiplier()).thenReturn(2);
        when(gameInfo.getGroup()).thenReturn(GameGroup.VIDEOPOKER);
        mockConverterData(coins);
        when(serverConfig.isDropFreeBalance()).thenReturn(false);
        when(serverConfig.getFreeBalance()).thenReturn(serverFreeBalanceProperty);

        long freeBalance = freeGameCalculator.calculateFreeBalance(gameInfo, "EUR");

        assertEquals("Should equals with server freeBalance property value if MaxBetInCredit property missing",
                serverFreeBalanceProperty, freeBalance);
    }

    @Test
    public void freeBalanceMultiplierInGameServerConfigWithVideoPokerGameWhenMaxBetInCreditExist() {
        int serverFBMpProperty = 2;
        int maxCoinValue = 50;
        List<Coin> coins = Collections.singletonList(Coin.getByValue(maxCoinValue));
        String maxBetInCreditProperty = "30";
        when(serverConfig.getFreeBalanceMultiplier()).thenReturn(serverFBMpProperty);
        when(gameInfo.getGroup()).thenReturn(GameGroup.VIDEOPOKER);
        mockConverterData(coins);
        when(gameInfo.getProperty(BaseGameConstants.KEY_MAX_BET_IN_CREDITS)).thenReturn(maxBetInCreditProperty);

        long freeBalance = freeGameCalculator.calculateFreeBalance(gameInfo, "EUR");

        assertEquals("Should equals rounded product of server freeBalanceMultiplier, max coins and present stakes reserve values",
                freeGameCalculator.getRoundBalance(maxCoinValue * Long.parseLong(maxBetInCreditProperty) * serverFBMpProperty), freeBalance);
    }

    @Test
    public void freeBalanceMultiplierInGameServerConfigWithSlotsGame() {
        int serverFBMpProperty = 2;
        int maxCoinValue = 50;
        List<Coin> coins = Collections.singletonList(Coin.getByValue(maxCoinValue));
        int maxBetInCredit = 30;
        when(serverConfig.getFreeBalanceMultiplier()).thenReturn(serverFBMpProperty);
        when(gameInfo.getGroup()).thenReturn(GameGroup.SLOTS);
        mockConverterData(coins);
        when(gameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong())).thenReturn(baseGameInfoTemplate);
        when(baseGameInfoTemplate.getMaxCredits()).thenReturn(maxBetInCredit);

        long freeBalance = freeGameCalculator.calculateFreeBalance(gameInfo, "EUR");

        assertEquals("Should equals rounded product of server freeBalanceMultiplier, max coins and max credit values",
                freeGameCalculator.getRoundBalance(maxCoinValue * maxBetInCredit * serverFBMpProperty), freeBalance);
    }

    @Test
    public void freeBalanceMultiplierInGameServerConfigWithUnknownGameGroup() {
        final long serverFreeBalance = 888L;
        when(serverConfig.getFreeBalanceMultiplier()).thenReturn(2);
        when(serverConfig.getFreeBalance()).thenReturn(serverFreeBalance);
        when(gameInfo.getGroup()).thenReturn(GameGroup.LIVE);

        long freeBalance = freeGameCalculator.calculateFreeBalance(gameInfo, "EUR");

        assertEquals("Should equals with server freeBalance property value when unknown game group", serverFreeBalance, freeBalance);
    }

    @Test
    public void freeBalanceMultiplierInBankConfigWithTableGame() {
        int bankFBMpProperty = 2;
        when(bankInfo.getFreeBalanceMultiplier()).thenReturn(bankFBMpProperty);
        when(gameInfo.getGroup()).thenReturn(GameGroup.TABLE);
        when(gameInfo.getLimit()).thenReturn(Limit.getByValue(300, 9900));

        long freeBalance = freeGameCalculator.calculateFreeBalance(gameInfo, "EUR");

        assertEquals("Should equals rounded product of bank freeBalanceMultiplier property and max limit values",
                freeGameCalculator.getRoundBalance(9900 * bankFBMpProperty), freeBalance);
    }

    @Test
    public void freeBalanceInBankConfigWithTableGame() {
        long bankFreeBalance = 12000;
        when(bankInfo.getFreeBalance()).thenReturn(bankFreeBalance);

        long freeBalance = freeGameCalculator.calculateFreeBalance(gameInfo, "EUR");

        assertEquals("Should equals with bank freeBalance property value", bankFreeBalance, freeBalance);
    }

    @Test
    public void freeBalanceMultiplierInGameConfigWithTableGame() {
        String gameFBMpProperty = "5";
        when(gameInfo.getProperty(KEY_FREEBALANCE)).thenReturn(null);
        when(gameInfo.getProperty(KEY_FREEBALANCE_MULTIPLIER)).thenReturn(gameFBMpProperty);
        when(gameInfo.getGroup()).thenReturn(GameGroup.TABLE);
        when(gameInfo.getLimit()).thenReturn(Limit.getByValue(300, 9900));

        long freeBalance = freeGameCalculator.calculateFreeBalance(gameInfo, "EUR");

        assertEquals("Should equals rounded product of game freeBalanceMultiplier property and max limit values",
                freeGameCalculator.getRoundBalance(9900 * Long.parseLong(gameFBMpProperty)), freeBalance);
    }

    @Test
    public void freeBalanceInGameConfigWithTableGame() {
        String gameFreeBalance = "70000";
        when(gameInfo.getProperty(KEY_FREEBALANCE)).thenReturn(gameFreeBalance);

        long freeBalance = freeGameCalculator.calculateFreeBalance(gameInfo, "EUR");

        assertEquals("Should equals with game freeBalance property value", Long.parseLong(gameFreeBalance), freeBalance);
    }

    private void mockConverterData(List<Coin> coins) {
        when(gameSettingsManager.getCoins(any(BaseGameInfo.class), anyString(), anyBoolean())).thenReturn(coins);
    }

    private void mockDropFreeBalance() {
        when(gameInfo.getProperty(any())).thenReturn(null);
        when(serverConfig.getFreeBalanceMultiplier()).thenReturn(null);
        when(serverConfig.isDropFreeBalance()).thenReturn(true);
    }

    @Test
    public void testRounding() {
        assertEquals(200, freeGameCalculator.getRoundBalance(199));
        assertEquals(2000, freeGameCalculator.getRoundBalance(2000));
        assertEquals(9000, freeGameCalculator.getRoundBalance(8001));
        assertEquals(10000, freeGameCalculator.getRoundBalance(9001));
    }
}
