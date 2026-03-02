package com.abs.casino.gs.managers.game.settings;

import com.abs.casino.common.SessionHelper;
import com.abs.casino.common.cache.BaseGameInfoTemplateCache;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.bank.Coin;
import com.abs.casino.common.cache.data.currency.Currency;
import com.abs.casino.common.cache.data.game.BaseGameConstants;
import com.abs.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.abs.casino.common.cache.data.game.IBaseGameInfo;
import com.abs.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.currency.ICurrencyRateManager;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.dblink.DBLink;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:dader@dgphoenix.com">Timur Shaymardanov</a>
 * @since 22.09.2020
 */
@SuppressWarnings("rawtypes")
@RunWith(MockitoJUnitRunner.class)
public class GameSettingsManagerTest {

    @Mock
    private ICurrencyRateManager currencyConverter;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private BaseGameInfoTemplateCache baseGameInfoTemplateCache;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SessionHelper sessionHelper;
    @Mock
    private DynamicCoinManager dynamicCoinManager;

    private GameSettingsManager gameSettingsManager;

    @Mock
    private GameSession gameSession;

    @Before
    public void setUp() throws Exception {
        gameSettingsManager = spy(new GameSettingsManager(currencyConverter, baseGameInfoTemplateCache, sessionHelper, dynamicCoinManager));
        when(sessionHelper.getTransactionData().getGameSession()).thenReturn(gameSession);
        when(gameSession.getCachedDynamicCoins()).thenReturn(null);
    }

    @Test
    public void testGetCoinsNotDynamic() {
        List<Coin> gameInfoCoins = Arrays.asList(
                Coin.getById(1L),
                Coin.getById(2L)
        );
        DBLink dbLink = mock(DBLink.class, RETURNS_DEEP_STUBS);
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class, RETURNS_DEEP_STUBS);
        when(dbLink.getGameSettings()).thenReturn(gameInfo);
        when(dbLink.getCurrency().getCode()).thenReturn("EUR");
        when(gameInfo.getId()).thenReturn(1L);
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(false);
        when(gameInfo.getCoins()).thenReturn(gameInfoCoins);

        List<Coin> coins = gameSettingsManager.getCoins(dbLink);

        assertThat(coins, containsInAnyOrder(gameInfoCoins.toArray()));
    }

    @Test
    public void testGetCoinsDynamicallyDBLink() throws CommonException {
        List<Coin> gameInfoCoins = Arrays.asList(
                Coin.getById(1L),
                Coin.getById(2L)
        );
        DBLink dbLink = mock(DBLink.class, RETURNS_DEEP_STUBS);
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class, RETURNS_DEEP_STUBS);
        when(dbLink.getGameSettings()).thenReturn(gameInfo);
        String targetCurrency = "EUR";
        when(dbLink.getCurrency().getCode()).thenReturn(targetCurrency);
        when(gameInfo.getId()).thenReturn(1L);
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(true);
        when(gameInfo.getCoins()).thenReturn(gameInfoCoins);
        List<Coin> expectedCoins = Arrays.asList(
                Coin.getById(10L),
                Coin.getById(11L),
                Coin.getById(12L),
                Coin.getById(13L)
        );
        when(dynamicCoinManager.getDynamicCoins(any(), anyString(), any())).thenReturn(expectedCoins);

        List<Coin> coins = gameSettingsManager.getCoins(dbLink);

        assertThat(coins, containsInAnyOrder(expectedCoins.toArray()));
        verify(sessionHelper, atLeast(2)).getTransactionData();
    }

    @Test
    public void testGetCoinsDynamicallyWithoutCache() throws CommonException {
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class);
        String targetCurrency = "EUR";
        List<Coin> gameInfoCoins = Arrays.asList(
                Coin.getById(1L),
                Coin.getById(2L),
                Coin.getById(3L),
                Coin.getById(4L)
        );
        when(gameInfo.getId()).thenReturn(1L);
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(true);
        when(gameInfo.getCoins()).thenReturn(gameInfoCoins);
        List<Coin> expectedCoins = Arrays.asList(
                Coin.getById(1),
                Coin.getById(2),
                Coin.getById(3)
        );
        when(dynamicCoinManager.getDynamicCoins(any(), anyString(), any())).thenReturn(expectedCoins);

        List<Coin> actualCoins = gameSettingsManager.getCoins(gameInfo, targetCurrency, false);

        assertThat(actualCoins, containsInAnyOrder(expectedCoins.toArray()));
        verify(sessionHelper, times(1)).getTransactionData();
    }

    @Test
    public void testGetCoinsWithMaxCoinLimitZeroRate() throws CommonException {
        List<Coin> gameInfoCoins = Arrays.asList(
                Coin.getById(1L),
                Coin.getById(2L),
                Coin.getById(3L),
                Coin.getById(4L)
        );
        String targetCurrency = "USD";
        DBLink dbLink = mock(DBLink.class, RETURNS_DEEP_STUBS);
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class, RETURNS_DEEP_STUBS);
        when(dbLink.getGameSettings()).thenReturn(gameInfo);
        when(dbLink.getCurrency().getCode()).thenReturn(targetCurrency);
        when(gameInfo.getId()).thenReturn(1L);
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(false);
        when(gameInfo.getCoins()).thenReturn(gameInfoCoins);
        String maxCoinLimitEurCentsStr = "1";
        when(gameInfo.getProperty(BaseGameConstants.KEY_MAX_COIN_LIMIT_EUR)).thenReturn(maxCoinLimitEurCentsStr);
        double zeroRate = 0;
        when(currencyConverter.getRateToBaseCurrency(targetCurrency)).thenReturn(zeroRate);

        List<Coin> coins = gameSettingsManager.getCoins(dbLink);

        assertThat(coins, containsInAnyOrder(gameInfoCoins.toArray()));
    }

    @Test
    public void testGetCoinsWithMaxCoinLimitErrorWithRate() throws CommonException {
        List<Coin> gameInfoCoins = Arrays.asList(
                Coin.getById(1L),
                Coin.getById(2L),
                Coin.getById(3L),
                Coin.getById(4L)
        );
        String targetCurrency = "USD";
        DBLink dbLink = mock(DBLink.class, RETURNS_DEEP_STUBS);
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class, RETURNS_DEEP_STUBS);
        when(dbLink.getGameSettings()).thenReturn(gameInfo);
        when(dbLink.getCurrency().getCode()).thenReturn(targetCurrency);
        when(gameInfo.getId()).thenReturn(1L);
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(false);
        when(gameInfo.getCoins()).thenReturn(gameInfoCoins);
        String maxCoinLimitEurCentsStr = "1";
        when(gameInfo.getProperty(BaseGameConstants.KEY_MAX_COIN_LIMIT_EUR)).thenReturn(maxCoinLimitEurCentsStr);
        doThrow(Exception.class).when(currencyConverter).getRateToBaseCurrency(targetCurrency);

        List<Coin> coins = gameSettingsManager.getCoins(dbLink);

        assertThat(coins, containsInAnyOrder(gameInfoCoins.toArray()));
    }

    @Test
    public void testGetCoinsWithMaxCoinLimitNotFiltered() throws CommonException {
        List<Coin> gameInfoCoins = Arrays.asList(
                Coin.getById(1L),
                Coin.getById(2L),
                Coin.getById(3L),
                Coin.getById(4L)
        );
        String targetCurrency = "USD";
        DBLink dbLink = mock(DBLink.class, RETURNS_DEEP_STUBS);
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class, RETURNS_DEEP_STUBS);
        when(dbLink.getGameSettings()).thenReturn(gameInfo);
        when(dbLink.getCurrency().getCode()).thenReturn(targetCurrency);
        when(gameInfo.getId()).thenReturn(1L);
        BaseGameInfoTemplate baseGameInfoTemplate = mock(BaseGameInfoTemplate.class);
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(false);
        when(gameInfo.getCoins()).thenReturn(gameInfoCoins);
        String maxCoinLimitEurCentsStr = "200";
        when(gameInfo.getProperty(BaseGameConstants.KEY_MAX_COIN_LIMIT_EUR)).thenReturn(maxCoinLimitEurCentsStr);
        double usdToEurRate = 1.5;
        when(currencyConverter.getRateToBaseCurrency(targetCurrency)).thenReturn(usdToEurRate);
        List<Coin> expectedCoins = Arrays.asList(
                Coin.getById(1L),
                Coin.getById(2L),
                Coin.getById(3L),
                Coin.getById(4L)
        );

        List<Coin> coins = gameSettingsManager.getCoins(dbLink);

        assertThat(coins, containsInAnyOrder(expectedCoins.toArray()));
    }

    @Test
    public void testGetCoinsWithMaxCoinLimitFiltered() throws CommonException {
        List<Coin> gameInfoCoins = Arrays.asList(
                Coin.getById(1L),
                Coin.getById(2L),
                Coin.getById(3L),
                Coin.getById(4L)
        );
        String targetCurrency = "USD";
        DBLink dbLink = mock(DBLink.class, RETURNS_DEEP_STUBS);
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class, RETURNS_DEEP_STUBS);
        when(dbLink.getGameSettings()).thenReturn(gameInfo);
        when(dbLink.getCurrency().getCode()).thenReturn(targetCurrency);
        when(gameInfo.getId()).thenReturn(1L);
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(false);
        when(gameInfo.getCoins()).thenReturn(gameInfoCoins);
        String maxCoinLimitEurCentsStr = "50";
        when(gameInfo.getProperty(BaseGameConstants.KEY_MAX_COIN_LIMIT_EUR)).thenReturn(maxCoinLimitEurCentsStr);
        double usdToEurRate = 1.5;
        when(currencyConverter.getRateToBaseCurrency(targetCurrency)).thenReturn(usdToEurRate);
        List<Coin> expectedCoins = Arrays.asList(
                Coin.getById(1L),
                Coin.getById(2L)
        );

        List<Coin> coins = gameSettingsManager.getCoins(dbLink);

        assertThat(coins, containsInAnyOrder(expectedCoins.toArray()));
    }

    @Test
    public void testGetCoinsWithMaxCoinLimitFilterAll() throws CommonException {
        List<Coin> gameInfoCoins = Arrays.asList(
                Coin.getById(1L),
                Coin.getById(2L),
                Coin.getById(3L),
                Coin.getById(4L)
        );
        String targetCurrency = "USD";
        DBLink dbLink = mock(DBLink.class, RETURNS_DEEP_STUBS);
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class, RETURNS_DEEP_STUBS);
        when(dbLink.getGameSettings()).thenReturn(gameInfo);
        when(dbLink.getCurrency().getCode()).thenReturn(targetCurrency);
        when(gameInfo.getId()).thenReturn(1L);
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(false);
        when(gameInfo.getCoins()).thenReturn(gameInfoCoins);
        String maxCoinLimitEurCentsStr = "10";
        when(gameInfo.getProperty(BaseGameConstants.KEY_MAX_COIN_LIMIT_EUR)).thenReturn(maxCoinLimitEurCentsStr);
        double usdToEurRate = 1.1;
        when(currencyConverter.getRateToBaseCurrency(targetCurrency)).thenReturn(usdToEurRate);
        List<Coin> expectedCoins = Collections.emptyList();

        List<Coin> coins = gameSettingsManager.getCoins(dbLink);

        assertThat(coins, containsInAnyOrder(expectedCoins.toArray()));
    }

    @Test
    public void testGetDefaultCoinNotDynamicFromDBLink() {
        DBLink dbLink = mock(DBLink.class, RETURNS_DEEP_STUBS);
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class);
        when(dbLink.getGameSettings()).thenReturn(gameInfo);
        String targetCurrency = "EUR";
        when(dbLink.getCurrency().getCode()).thenReturn(targetCurrency);
        Integer gameDefaultCoin = 5;
        when(gameInfo.getDefaultCoin()).thenReturn(gameDefaultCoin);
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(false);

        Integer actualDefaultCoin = gameSettingsManager.getDefaultCoin(dbLink);

        assertEquals(gameDefaultCoin, actualDefaultCoin);
    }

    @Test
    public void testGetDefaultCoinDynamicFromDBLink() throws CommonException {
        DBLink dbLink = mock(DBLink.class, RETURNS_DEEP_STUBS);
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class);
        when(dbLink.getGameSettings()).thenReturn(gameInfo);
        String targetCurrency = "EUR";
        when(dbLink.getCurrency().getCode()).thenReturn(targetCurrency);
        Integer gameDefaultCoin = 5;
        when(gameInfo.getDefaultCoin()).thenReturn(gameDefaultCoin);
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(true);
        Integer expectedDefaultCoin = 3;
        when(dynamicCoinManager.getDynamicDefaultCoin(any(), anyString(), any(), any())).thenReturn(Optional.of(expectedDefaultCoin));

        Integer actualDefaultCoin = gameSettingsManager.getDefaultCoin(dbLink);

        assertEquals(expectedDefaultCoin, actualDefaultCoin);
    }

    @Test
    public void testGetDefaultCoinDynamicFromDBLinkIsNull() throws CommonException {
        DBLink dbLink = mock(DBLink.class, RETURNS_DEEP_STUBS);
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class);
        when(dbLink.getGameSettings()).thenReturn(gameInfo);
        String targetCurrency = "EUR";
        when(dbLink.getCurrency().getCode()).thenReturn(targetCurrency);
        when(gameInfo.getDefaultCoin()).thenReturn(null);
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(true);
        when(dynamicCoinManager.getDynamicDefaultCoin(any(), anyString(), any(), any())).thenReturn(Optional.empty());

        Integer actualDefaultCoin = gameSettingsManager.getDefaultCoin(dbLink);

        assertNull(actualDefaultCoin);
    }

    @Test
    public void testGetDefaultCoinNotDynamic() {
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class);
        String targetCurrency = "EUR";
        Integer expectedDefaultCoin = 3;
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(false);
        when(gameInfo.getDefaultCoin()).thenReturn(expectedDefaultCoin);

        Integer actualDefaultCoin = gameSettingsManager.getDefaultCoin(gameInfo, targetCurrency);

        assertEquals(expectedDefaultCoin, actualDefaultCoin);
    }

    @Test
    public void testGetDefaultCoinDynamic() throws CommonException {
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class);
        String targetCurrency = "EUR";
        Integer expectedDefaultCoin = 2;
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(true);
        when(gameInfo.getDefaultCoin()).thenReturn(9);
        List<Coin> coins = Arrays.asList(
                Coin.getByValue(1L),
                Coin.getByValue(5L),
                Coin.getByValue(10L),
                Coin.getByValue(25L)
        );
        doReturn(coins).when(gameSettingsManager).getCoins(gameInfo, targetCurrency, false);
        double[] coinSequence = new double[]{0.01d, 0.05d, 0.1d, 0.25d};
        when(dynamicCoinManager.getDynamicDefaultCoin(eq(gameInfo), eq(targetCurrency), eq(coinSequence), any())).thenReturn(Optional.of(expectedDefaultCoin));

        Integer actualDefaultCoin = gameSettingsManager.getDefaultCoin(gameInfo, targetCurrency);

        assertEquals(expectedDefaultCoin, actualDefaultCoin);
    }

    @Test
    public void testGetDefaultCoinIsNull() throws CommonException {
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class);
        String targetCurrency = "EUR";
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(true);
        when(gameInfo.getDefaultCoin()).thenReturn(null);
        List<Coin> coins = Arrays.asList(
                Coin.getByValue(1L),
                Coin.getByValue(5L),
                Coin.getByValue(10L),
                Coin.getByValue(25L)
        );
        doReturn(coins).when(gameSettingsManager).getCoins(gameInfo, targetCurrency, false);
        double[] coinSequence = new double[]{0.01d, 0.05d, 0.1d, 0.25d};
        when(dynamicCoinManager.getDynamicDefaultCoin(eq(gameInfo), eq(targetCurrency), eq(coinSequence), any())).thenReturn(Optional.empty());

        Integer actualDefaultCoin = gameSettingsManager.getDefaultCoin(gameInfo, targetCurrency);

        assertNull(actualDefaultCoin);
    }

    @Test
    public void testGetDefaultCoinNotDynamicIgnoreProvidedCoins() {
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class);
        String targetCurrency = "EUR";
        Integer expectedDefaultCoin = 3;
        List<Coin> coins = Arrays.asList(
                Coin.getByValue(1L),
                Coin.getByValue(5L),
                Coin.getByValue(10L),
                Coin.getByValue(25L)
        );
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(false);
        when(gameInfo.getDefaultCoin()).thenReturn(expectedDefaultCoin);

        Integer actualDefaultCoin = gameSettingsManager.getDefaultCoin(gameInfo, targetCurrency, coins);

        assertEquals(expectedDefaultCoin, actualDefaultCoin);
    }

    @Test
    public void testGetDefaultCoinDynamicFromProvidedCoins() throws CommonException {
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class);
        String targetCurrency = "EUR";
        Integer expectedDefaultCoin = 2;
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(true);
        when(gameInfo.getDefaultCoin()).thenReturn(9);
        List<Coin> coins = Arrays.asList(
                Coin.getByValue(1L),
                Coin.getByValue(5L),
                Coin.getByValue(10L),
                Coin.getByValue(25L)
        );
        double[] coinSequence = new double[]{0.01d, 0.05d, 0.1d, 0.25d};
        when(dynamicCoinManager.getDynamicDefaultCoin(eq(gameInfo), eq(targetCurrency), eq(coinSequence), any())).thenReturn(Optional.of(expectedDefaultCoin));

        Integer actualDefaultCoin = gameSettingsManager.getDefaultCoin(gameInfo, targetCurrency, coins);

        assertEquals(expectedDefaultCoin, actualDefaultCoin);
    }

    @Test
    public void testGetDefaultCoinDynamicIsNullFromProvidedCoins() throws CommonException {
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class);
        String targetCurrency = "EUR";
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(true);
        when(gameInfo.getDefaultCoin()).thenReturn(null);
        List<Coin> coins = Arrays.asList(
                Coin.getByValue(1L),
                Coin.getByValue(5L),
                Coin.getByValue(10L),
                Coin.getByValue(25L)
        );
        double[] coinSequence = new double[]{0.01d, 0.05d, 0.1d, 0.25d};
        when(dynamicCoinManager.getDynamicDefaultCoin(eq(gameInfo), eq(targetCurrency), eq(coinSequence), any())).thenReturn(Optional.empty());

        Integer actualDefaultCoin = gameSettingsManager.getDefaultCoin(gameInfo, targetCurrency, coins);

        assertNull(actualDefaultCoin);
    }

    @Test
    public void testGetFRBCoinNotDynamic() {
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class);
        BankInfo bankInfo = mock(BankInfo.class);
        Currency targetCurrency = new Currency("EUR", "E");
        String expectedFRBCoin = "1";
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(false);
        when(gameInfo.getProperty(anyString())).thenReturn(expectedFRBCoin);

        String actualDefaultCoin = gameSettingsManager.getFRBCoin(bankInfo, gameInfo, targetCurrency);

        assertEquals(expectedFRBCoin, actualDefaultCoin);
    }

    @Test
    public void testGetFRBCoinForNonGLSlot() throws CommonException {
        BankInfo bankInfo = mock(BankInfo.class);
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class);
        Currency targetCurrency = new Currency("EUR", "E");
        Long expectedFRBCoin = 10L;
        when(bankInfo.isUseFRBBetForNonGLSlots()).thenReturn(true);
        when(dynamicCoinManager.getDynamicFrbCoin(eq(gameInfo), eq(targetCurrency), any())).thenReturn(Optional.of(expectedFRBCoin));

        String actualDefaultCoin = gameSettingsManager.getFRBCoin(bankInfo, gameInfo, targetCurrency);

        assertEquals(expectedFRBCoin.toString(), actualDefaultCoin);
    }

    @Test
    public void testStaticFrbCoinsIgnored() throws CommonException {
        BankInfo bankInfo = mock(BankInfo.class);
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class);
        Currency targetCurrency = new Currency("EUR", "E");
        Long expectedFRBCoin = 10L;
        when(bankInfo.isUseFRBBetForNonGLSlots()).thenReturn(true);
        when(bankInfo.getFRBBet()).thenReturn(1L);
        when(bankInfo.isStaticFrbCoinsIgnored()).thenReturn(true);
        when(dynamicCoinManager.getDynamicFrbCoin(eq(gameInfo), eq(targetCurrency), any())).thenReturn(Optional.of(expectedFRBCoin));

        String actualDefaultCoin = gameSettingsManager.getFRBCoin(bankInfo, gameInfo, targetCurrency);

        assertEquals(expectedFRBCoin.toString(), actualDefaultCoin);
    }

    @Test
    public void testGetFRBCoinNotConfiguredBetForBank() {
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class);
        BankInfo bankInfo = mock(BankInfo.class);
        Currency targetCurrency = new Currency("EUR", "E");
        String expectedFRBCoin = "1";
        when(gameInfo.getProperty(anyString())).thenReturn(expectedFRBCoin);
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong()).isDynamicLevelsSupported()).thenReturn(true);
        when(bankInfo.getFRBBet()).thenReturn(null);

        String actualDefaultCoin = gameSettingsManager.getFRBCoin(bankInfo, gameInfo, targetCurrency);

        assertEquals(expectedFRBCoin, actualDefaultCoin);
    }

}