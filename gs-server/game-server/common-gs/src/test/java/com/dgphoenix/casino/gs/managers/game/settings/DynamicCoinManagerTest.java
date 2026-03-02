package com.abs.casino.gs.managers.game.settings;

import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.BaseGameInfoTemplateCache;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.bank.Coin;
import com.abs.casino.common.cache.data.currency.Currency;
import com.abs.casino.common.cache.data.game.BaseGameConstants;
import com.abs.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.abs.casino.common.cache.data.game.GameVariableType;
import com.abs.casino.common.cache.data.game.IBaseGameInfo;
import com.abs.casino.common.currency.ICurrencyRateManager;
import com.abs.casino.common.exception.CommonException;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
 * @since 24.09.2020
 */
@RunWith(MockitoJUnitRunner.class)
public class DynamicCoinManagerTest {

    @Mock
    private BankInfoCache bankInfoCache;
    @Mock
    private BaseGameInfoTemplateCache baseGameInfoTemplateCache;
    @Mock
    private ICurrencyRateManager currencyConverter;
    @Mock
    private GamesLevelHelper gamesLevelHelper;

    private DynamicCoinManager dynamicCoinManager;

    @Before
    public void setUp() throws Exception {
        dynamicCoinManager = spy(new DynamicCoinManager(bankInfoCache, baseGameInfoTemplateCache, currencyConverter, gamesLevelHelper));
    }

    @Test
    public void testGetDynamicCoins() throws CommonException {
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class, RETURNS_DEEP_STUBS);
        BaseGameInfoTemplate template = mock(BaseGameInfoTemplate.class);
        BankInfo bankInfo = mock(BankInfo.class, RETURNS_DEEP_STUBS);
        when(gameInfo.getCurrency().getCode()).thenReturn("USD");
        when(bankInfoCache.getBankInfo(anyLong())).thenReturn(bankInfo);
        when(baseGameInfoTemplateCache.getBaseGameInfoTemplateById(anyLong())).thenReturn(template);
        List<Coin> expectedCoins = Arrays.asList(
                Coin.getById(1),
                Coin.getById(2),
                Coin.getById(3),
                Coin.getById(4)
        );
        when(gamesLevelHelper.filterCoinsByNumber(anyList(), anyInt())).thenReturn(expectedCoins);
        String targetCurrency = "USD";


        List<Coin> actualCoins = dynamicCoinManager.getDynamicCoins(gameInfo, targetCurrency, (c1, c2) -> true);

        assertThat(actualCoins, containsInAnyOrder(expectedCoins.toArray()));
    }

    @Test
    public void testGetDynamicDefaultCoin() throws CommonException {
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class, RETURNS_DEEP_STUBS);
        BankInfo bankInfo = mock(BankInfo.class, RETURNS_DEEP_STUBS);
        when(bankInfoCache.getBankInfo(anyLong())).thenReturn(bankInfo);
        when(bankInfo.isGLUseDefaultCurrency()).thenReturn(true);
        String sourceCurrency = "USD";
        when(bankInfo.getDefaultCurrency().getCode()).thenReturn(sourceCurrency);
        Long bankDefaultBet = 500L;
        when(bankInfo.getDefaultBet()).thenReturn(bankDefaultBet);
        ImmutableMap<String, String> gameProperties = ImmutableMap.of(
                BaseGameConstants.KEY_GL_DEFAULT_BET, "750"
        );
        when(gameInfo.getProperties()).thenReturn(gameProperties);
        String gameCurrency = "RUB";
        when(gameInfo.getCurrency().getCode()).thenReturn(gameCurrency);
        double finalDefaultBet = 250;
        when(currencyConverter.convert(anyDouble(), anyString(), anyString())).thenReturn(finalDefaultBet);
        String gameDefaultNumLines = "11";
        when(gameInfo.getProperty(anyString())).thenReturn(gameDefaultNumLines);
        double[] coinseq = {3, 4, 1, 2};
        int actualCoinIndex = 2;

        Optional<Integer> actualDefaultCoin = dynamicCoinManager.getDynamicDefaultCoin(gameInfo, "EUR", coinseq, (s1, s2) -> true);

        assertTrue(actualDefaultCoin.isPresent());
        assertEquals(actualCoinIndex, actualDefaultCoin.get().intValue());
    }

    @Test
    public void testGetDynamicDefaultCoinEmpty() throws CommonException {
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class, RETURNS_DEEP_STUBS);
        BankInfo bankInfo = mock(BankInfo.class, RETURNS_DEEP_STUBS);
        when(bankInfoCache.getBankInfo(anyLong())).thenReturn(bankInfo);
        when(bankInfo.isGLUseDefaultCurrency()).thenReturn(true);
        String sourceCurrency = "USD";
        when(bankInfo.getDefaultCurrency().getCode()).thenReturn(sourceCurrency);
        when(bankInfo.getDefaultBet()).thenReturn(null);
        when(gameInfo.getProperties()).thenReturn(Collections.emptyMap());
        double[] coinseq = {3, 4, 1, 2};

        Optional<Integer> actualDefaultCoin = dynamicCoinManager.getDynamicDefaultCoin(gameInfo, "EUR", coinseq, (s1, s2) -> true);

        assertFalse(actualDefaultCoin.isPresent());
    }

    @Test
    public void testGetDynamicFrbCoin() throws CommonException {
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class, RETURNS_DEEP_STUBS);
        BankInfo bankInfo = mock(BankInfo.class, RETURNS_DEEP_STUBS);

        Currency usd = new Currency("USD", "U");
        when(gameInfo.getVariableType()).thenReturn(GameVariableType.COIN);
        when(gameInfo.getProperty(BaseGameConstants.KEY_FRB_DEFAULTNUMLINES)).thenReturn("10");

        when(bankInfoCache.getBankInfo(anyLong())).thenReturn(bankInfo);
        when(bankInfo.isGLUseDefaultCurrency()).thenReturn(true);
        when(bankInfo.getDefaultCurrency().getCode()).thenReturn("EUR");
        when(currencyConverter.convert(anyDouble(), anyString(), anyString())).thenReturn(20.25d);
        Long expectedFRBCoin = 2L;

        Optional<Long> dynamicFrbCoin = dynamicCoinManager.getDynamicFrbCoin(gameInfo, usd, (c1, c2) -> true);

        assertTrue(dynamicFrbCoin.isPresent());
        assertEquals(expectedFRBCoin, dynamicFrbCoin.get());
    }

    @Test
    public void testGetDynamicFrbCoinNotUseForbiddenCoin() throws CommonException {
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class, RETURNS_DEEP_STUBS);
        BankInfo bankInfo = mock(BankInfo.class, RETURNS_DEEP_STUBS);

        Currency usd = new Currency("USD", "U");
        when(gameInfo.getVariableType()).thenReturn(GameVariableType.COIN);
        when(gameInfo.getProperty(BaseGameConstants.KEY_FRB_DEFAULTNUMLINES)).thenReturn("1");

        when(bankInfoCache.getBankInfo(anyLong())).thenReturn(bankInfo);
        when(bankInfo.isGLUseDefaultCurrency()).thenReturn(true);
        when(bankInfo.getDefaultCurrency().getCode()).thenReturn("EUR");
        when(currencyConverter.convert(anyDouble(), anyString(), anyString())).thenReturn(6210.25d);
        Long expectedFRBCoin = 6000L;

        Optional<Long> dynamicFrbCoin = dynamicCoinManager.getDynamicFrbCoin(gameInfo, usd, (c1, c2) -> true);

        assertTrue(dynamicFrbCoin.isPresent());
        assertEquals(expectedFRBCoin, dynamicFrbCoin.get());
    }

    @Test
    public void testGetDynamicFrbCoinNotUseUglyCoin() throws CommonException {
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class, RETURNS_DEEP_STUBS);
        BankInfo bankInfo = mock(BankInfo.class, RETURNS_DEEP_STUBS);

        Currency usd = new Currency("USD", "U");
        when(gameInfo.getVariableType()).thenReturn(GameVariableType.COIN);
        when(gameInfo.getProperty(BaseGameConstants.KEY_FRB_DEFAULTNUMLINES)).thenReturn("1");

        when(bankInfoCache.getBankInfo(anyLong())).thenReturn(bankInfo);
        when(bankInfo.isGLUseDefaultCurrency()).thenReturn(true);
        when(bankInfo.getDefaultCurrency().getCode()).thenReturn("EUR");
        when(currencyConverter.convert(anyDouble(), anyString(), anyString())).thenReturn(31049.25d);
        Long expectedFRBCoin = 30000L;

        Optional<Long> dynamicFrbCoin = dynamicCoinManager.getDynamicFrbCoin(gameInfo, usd, (c1, c2) -> true);

        assertTrue(dynamicFrbCoin.isPresent());
        assertEquals(expectedFRBCoin, dynamicFrbCoin.get());
    }

    @Test
    public void testGetDynamicFrbCoinNotAvailable() throws CommonException {
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class, RETURNS_DEEP_STUBS);
        BankInfo bankInfo = mock(BankInfo.class, RETURNS_DEEP_STUBS);

        Currency usd = new Currency("USD", "U");
        when(gameInfo.getVariableType()).thenReturn(GameVariableType.COIN);
        when(gameInfo.getProperty(BaseGameConstants.KEY_FRB_DEFAULTNUMLINES)).thenReturn(null);
        when(gameInfo.getProperty(BaseGameConstants.KEY_DEFAULTNUMLINES)).thenReturn("50");

        when(bankInfoCache.getBankInfo(anyLong())).thenReturn(bankInfo);
        when(bankInfo.isGLUseDefaultCurrency()).thenReturn(true);
        when(bankInfo.getDefaultCurrency().getCode()).thenReturn("EUR");
        when(currencyConverter.convert(anyDouble(), anyString(), anyString())).thenReturn(49.75d);

        Optional<Long> dynamicFrbCoin = dynamicCoinManager.getDynamicFrbCoin(gameInfo, usd, (c1, c2) -> true);

        assertFalse(dynamicFrbCoin.isPresent());
    }

}