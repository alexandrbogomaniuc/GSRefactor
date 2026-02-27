package com.abs.casino.gs.managers.game.settings;

import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.game.BaseGameConstants;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;
import java.util.function.Predicate;

import static com.dgphoenix.casino.common.cache.data.game.BaseGameConstants.*;
import static com.abs.casino.gs.managers.game.settings.GamesLevelHelper.forbiddenCoins;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:dader@dgphoenix.com">Timur Shaymardanov</a>
 * @since 23.09.2020
 */
@RunWith(MockitoJUnitRunner.class)
public class GamesLevelHelperTest {

    @Mock
    private ICurrencyRateManager currencyConverter;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private BaseGameInfoTemplate template;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private BankInfo bankInfo;

    private GamesLevelHelper helper;

    @Before
    public void setUp() throws Exception {
        helper = new GamesLevelHelper(currencyConverter);
    }

    @Test
    public void testGetGLMinBetTemplateMinCredits() throws CommonException {
        double expectedMinBet = 5;

        when(template.getMinCredits()).thenReturn(5);
        when(template.getMinBet()).thenReturn(null);
        when(bankInfo.getMinBet()).thenReturn(null);
        GamesLevelContext gamesLevelContext = getContext(Collections.emptyMap(), "RUB", "USD", true, "EUR", false);

        double actualMinBet = helper.getGLMinBet(gamesLevelContext);

        assertEquals("GL Min Bet should be equal to expected", expectedMinBet, actualMinBet, 0.0001);
    }

    @Test
    public void testGetGLMinBetTemplateMinBetWithoutConversion() throws CommonException {
        double expectedMinBet = 10;
        when(template.getMinCredits()).thenReturn(5);
        when(template.getMinBet()).thenReturn(10L);
        when(bankInfo.getMinBet()).thenReturn(null);
        GamesLevelContext gamesLevelContext = getContext(Collections.emptyMap(), "RUB", "USD", true, "EUR", false);

        double actualMinBet = helper.getGLMinBet(gamesLevelContext);

        assertEquals("GL Min Bet should be equal to expected", expectedMinBet, actualMinBet, 0.0001);
    }

    @Test
    public void testGetGLMinBetTemplateMinBetWithConversion() throws CommonException {
        double expectedMinBet = 15;
        when(template.getMinCredits()).thenReturn(5);
        when(template.getMinBet()).thenReturn(10L);
        when(bankInfo.getMinBet()).thenReturn(null);
        when(currencyConverter.convert(10.0, "EUR", "USD")).thenReturn(expectedMinBet);
        GamesLevelContext gamesLevelContext = getContext(Collections.emptyMap(), "RUB", "USD", true, "EUR", true);

        double actualMinBet = helper.getGLMinBet(gamesLevelContext);

        assertEquals("GL Min Bet should be equal to expected", expectedMinBet, actualMinBet, 0.0001);
    }

    @Test
    public void testGetGLMinBetBankMinBetWithoutConversion() throws CommonException {
        double expectedMinBet = 20;
        when(template.getMinCredits()).thenReturn(5);
        when(template.getMinBet()).thenReturn(10L);
        when(bankInfo.getMinBet()).thenReturn(20L);
        GamesLevelContext gamesLevelContext = getContext(Collections.emptyMap(), "RUB", "USD", true, "EUR", false);

        double actualMinBet = helper.getGLMinBet(gamesLevelContext);

        assertEquals("GL Min Bet should be equal to expected", expectedMinBet, actualMinBet, 0.0001);
    }

    @Test
    public void testGetGLMinBetBankMinBetWithConversion() throws CommonException {
        double expectedMinBet = 20;
        when(template.getMinCredits()).thenReturn(5);
        when(template.getMinBet()).thenReturn(10L);
        when(bankInfo.getMinBet()).thenReturn(20L);
        when(currencyConverter.convert(20.0, "USD", "USD")).thenReturn(expectedMinBet);
        GamesLevelContext gamesLevelContext = getContext(Collections.emptyMap(), "RUB", "USD", true, "EUR", true);

        double actualMinBet = helper.getGLMinBet(gamesLevelContext);

        assertEquals("GL Min Bet should be equal to expected", expectedMinBet, actualMinBet, 0.0001);
    }

    @Test
    public void testGetGLMinBetGameMinBetWithoutConversion() throws CommonException {
        double expectedMinBet = 30;
        when(template.getMinCredits()).thenReturn(5);
        when(template.getMinBet()).thenReturn(10L);
        when(bankInfo.getMinBet()).thenReturn(20L);
        GamesLevelContext gamesLevelContext = getContext(ImmutableMap.of(KEY_GL_MIN_BET, "30"), "RUB", "USD", true, "EUR", false);

        double actualMinBet = helper.getGLMinBet(gamesLevelContext);

        assertEquals("GL Min Bet should be equal to expected", expectedMinBet, actualMinBet, 0.0001);
    }

    @Test
    public void testGetGLMinBetGameMinBetWithConversion() throws CommonException {
        double expectedMinBet = 35;
        when(template.getMinCredits()).thenReturn(5);
        when(template.getMinBet()).thenReturn(10L);
        when(bankInfo.getMinBet()).thenReturn(20L);
        when(currencyConverter.convert(30.0, "RUB", "USD")).thenReturn(expectedMinBet);
        GamesLevelContext gamesLevelContext = getContext(ImmutableMap.of(KEY_GL_MIN_BET, "30"), "RUB", "USD", true, "EUR", true);

        double actualMinBet = helper.getGLMinBet(gamesLevelContext);

        assertEquals("GL Min Bet should be equal to expected", expectedMinBet, actualMinBet, 0.0001);
    }

    @Test
    public void testGetGLMaxBetTemplateMaxCredits() throws CommonException {
        double expectedMaxBet = 500;
        when(template.getMaxCredits()).thenReturn(5);
        when(template.getMaxBet()).thenReturn(null);
        when(bankInfo.getMaxBet()).thenReturn(null);
        GamesLevelContext context = getContext(Collections.emptyMap(), "RUB", "USD", true, "EUR", false);

        double actualMaxBet = helper.getGLMaxBet(context);

        assertEquals(expectedMaxBet, actualMaxBet, 0.0001);
    }

    @Test
    public void testGetGLMaxBetTemplateMaxBetWithoutConversion() throws CommonException {
        double expectedMaxBet = 10;
        when(template.getMaxCredits()).thenReturn(5);
        when(template.getMaxBet()).thenReturn(10L);
        when(bankInfo.getMaxBet()).thenReturn(null);
        GamesLevelContext context = getContext(Collections.emptyMap(), "RUB", "USD", true, "EUR", false);

        double actualMaxBet = helper.getGLMaxBet(context);

        assertEquals(expectedMaxBet, actualMaxBet, 0.0001);
    }

    @Test
    public void testGetGLMaxBetTemplateMaxBetWithConversion() throws CommonException {
        double expectedMaxBet = 15;
        when(template.getMaxCredits()).thenReturn(5);
        when(template.getMaxBet()).thenReturn(10L);
        when(bankInfo.getMaxBet()).thenReturn(null);
        when(currencyConverter.convert(10.0, "EUR", "USD")).thenReturn(expectedMaxBet);
        GamesLevelContext context = getContext(Collections.emptyMap(), "RUB", "USD", true, "EUR", true);

        double actualMaxBet = helper.getGLMaxBet(context);

        assertEquals(expectedMaxBet, actualMaxBet, 0.0001);
    }

    @Test
    public void testGetGLMaxBetBankMaxBetWithoutConversion() throws CommonException {
        double expectedMaxBet = 20;
        when(template.getMaxCredits()).thenReturn(5);
        when(template.getMaxBet()).thenReturn(10L);
        when(bankInfo.getMaxBet()).thenReturn(20L);
        GamesLevelContext context = getContext(Collections.emptyMap(), "RUB", "USD", true, "EUR", false);

        double actualMaxBet = helper.getGLMaxBet(context);

        assertEquals(expectedMaxBet, actualMaxBet, 0.0001);
    }

    @Test
    public void testGetGLMaxBetBankMaxBetWithConversion() throws CommonException {
        double expectedMaxBet = 20;
        when(template.getMaxCredits()).thenReturn(5);
        when(template.getMaxBet()).thenReturn(10L);
        when(bankInfo.getMaxBet()).thenReturn(20L);
        when(currencyConverter.convert(20.0, "USD", "USD")).thenReturn(expectedMaxBet);
        GamesLevelContext context = getContext(Collections.emptyMap(), "RUB", "USD", true, "EUR", true);

        double actualMaxBet = helper.getGLMaxBet(context);

        assertEquals(expectedMaxBet, actualMaxBet, 0.0001);
    }

    @Test
    public void testGetGLMaxBetGameMaxBetWithoutConversion() throws CommonException {
        double expectedMaxBet = 30;
        when(template.getMaxCredits()).thenReturn(5);
        when(template.getMaxBet()).thenReturn(10L);
        when(bankInfo.getMaxBet()).thenReturn(20L);
        GamesLevelContext context = getContext(ImmutableMap.of(KEY_GL_MAX_BET, "30"), "RUB", "USD", true, "EUR", false);

        double actualMaxBet = helper.getGLMaxBet(context);

        assertEquals(expectedMaxBet, actualMaxBet, 0.0001);
    }

    @Test
    public void testGetGLMaxBetGameMaxBetWithConversion() throws CommonException {
        double expectedMaxBet = 35;
        when(template.getMaxCredits()).thenReturn(5);
        when(template.getMaxBet()).thenReturn(10L);
        when(bankInfo.getMaxBet()).thenReturn(20L);
        when(currencyConverter.convert(30.0, "RUB", "USD")).thenReturn(expectedMaxBet);
        GamesLevelContext context = getContext(ImmutableMap.of(KEY_GL_MAX_BET, "30"), "RUB", "USD", true, "EUR", true);

        double actualMaxBet = helper.getGLMaxBet(context);

        assertEquals(expectedMaxBet, actualMaxBet, 0.0001);
    }

    @Test
    public void testLimitMaxBetAllowedMaxWinTheSame() throws CommonException {
        double expectedMaxWin = 20;
        when(bankInfo.getMaxWin()).thenReturn(null);
        GamesLevelContext context = getContext(Collections.emptyMap(), "RUB", "USD", true, "EUR", false);

        double actualMaxWin = helper.limitGLMaxBetByAllowedMaxWin(context, 20);

        assertEquals(expectedMaxWin, actualMaxWin, 0.0001);
    }

    @Test
    public void testLimitMaxBetAllowedMaxWinBankWithoutConversion() throws CommonException {
        double expectedMaxWin = 6;
        when(bankInfo.getMaxWin()).thenReturn(10L);
        when(template.getDefaultGameInfo().getProperty(KEY_MAX_WIN)).thenReturn("5");
        when(template.getMaxCredits()).thenReturn(3);
        GamesLevelContext context = getContext(Collections.emptyMap(), "RUB", "USD", true, "EUR", false);

        double actualMaxWin = helper.limitGLMaxBetByAllowedMaxWin(context, 20);

        assertEquals(expectedMaxWin, actualMaxWin, 0.0001);
    }

    @Test
    public void testLimitMaxBetAllowedMaxWinBankWithConversion() throws CommonException {
        double expectedMaxWin = 9;
        when(bankInfo.getMaxWin()).thenReturn(10L);
        when(currencyConverter.convert(10.0, "USD", "EUR")).thenReturn(15.0);
        when(template.getDefaultGameInfo().getProperty(KEY_MAX_WIN)).thenReturn("5");
        when(template.getMaxCredits()).thenReturn(3);
        GamesLevelContext context = getContext(Collections.emptyMap(), "EUR", "USD", true, "EUR", true);

        double actualMaxWin = helper.limitGLMaxBetByAllowedMaxWin(context, 20);

        assertEquals(expectedMaxWin, actualMaxWin, 0.0001);
    }

    @Test
    public void testLimitMaxBetAllowedMaxWinGameWithoutConversion() throws CommonException {
        double expectedMaxWin = 12;
        when(bankInfo.getMaxWin()).thenReturn(15L);
        when(template.getDefaultGameInfo().getProperty(KEY_MAX_WIN)).thenReturn("5");
        when(template.getMaxCredits()).thenReturn(3);
        GamesLevelContext context = getContext(ImmutableMap.of(KEY_GL_MAX_EXPOSURE, "20"), "EUR", "USD", true, "EUR", false);

        double actualMaxWin = helper.limitGLMaxBetByAllowedMaxWin(context, 20);

        assertEquals(expectedMaxWin, actualMaxWin, 0.0001);
    }

    @Test
    public void testLimitMaxBetAllowedMaxWinGameWithConversion() throws CommonException {
        double expectedMaxWin = 15;
        when(bankInfo.getMaxWin()).thenReturn(10L);
        when(currencyConverter.convert(20.0, "USD", "EUR")).thenReturn(25.0);
        when(template.getDefaultGameInfo().getProperty(KEY_MAX_WIN)).thenReturn("5");
        when(template.getMaxCredits()).thenReturn(3);
        GamesLevelContext context = getContext(ImmutableMap.of(KEY_GL_MAX_EXPOSURE, "20"), "USD", "RUB", false, "EUR", true);

        double actualMaxWin = helper.limitGLMaxBetByAllowedMaxWin(context, 20);

        assertEquals(expectedMaxWin, actualMaxWin, 0.0001);
    }

    @Test
    public void testGetGLCoinsNumberDefault() {
        int expectedCoinsNumber = 11;
        when(bankInfo.getCoinsNumber()).thenReturn(null);
        GamesLevelContext context = getContext(Collections.emptyMap(), "EUR", "USD", true, "EUR", true);

        int actualCoinsNumber = helper.getGLCoinsNumber(context);

        assertEquals(expectedCoinsNumber, actualCoinsNumber);
    }

    @Test
    public void testGetGLCoinsNumberBank() {
        int expectedCoinsNumber = 5;
        when(bankInfo.getCoinsNumber()).thenReturn(5);
        GamesLevelContext context = getContext(Collections.emptyMap(), "EUR", "USD", true, "EUR", true);

        int actualCoinsNumber = helper.getGLCoinsNumber(context);

        assertEquals(expectedCoinsNumber, actualCoinsNumber);
    }

    @Test
    public void testGetGLCoinsNumberGame() {
        int expectedCoinsNumber = 8;
        when(bankInfo.getCoinsNumber()).thenReturn(5);
        GamesLevelContext context = getContext(ImmutableMap.of(KEY_GL_NUMBER_OF_COINS, "8"), "EUR", "USD", true, "EUR", true);

        int actualCoinsNumber = helper.getGLCoinsNumber(context);

        assertEquals(expectedCoinsNumber, actualCoinsNumber);
    }

    @Test
    public void testGetAvailableCoinsFilterByMaxBet() throws CommonException {
        double glMaxBet = 150.0;
        double glMinBet = 0.0;
        String targetCurrency = "EUR";
        int maxCredits = 20;
        int minCredits = 5;
        when(template.getMaxCredits()).thenReturn(maxCredits);
        when(template.getMinCredits()).thenReturn(minCredits);
        GamesLevelContext context = getContext(Collections.emptyMap(), "EUR", "USD", true, targetCurrency, false);

        List<Coin> availableCoins = helper.getAvailableCoins(context, glMinBet, glMaxBet, targetCurrency);

        long maxAllowedCoinValue = (long) Math.floor(glMaxBet / maxCredits);
        assertThat(availableCoins, everyItem(hasProperty("value", lessThanOrEqualTo(maxAllowedCoinValue))));
    }

    @Test
    public void testGetAvailableCoinsFilterByMinBet() throws CommonException {
        double glMaxBet = Double.MAX_VALUE;
        double glMinBet = 50;
        int maxCredits = 20;
        int minCredits = 5;
        when(template.getMaxCredits()).thenReturn(maxCredits);
        when(template.getMinCredits()).thenReturn(minCredits);
        String targetCurrency = "EUR";
        GamesLevelContext context = getContext(Collections.emptyMap(), "EUR", "USD", true, targetCurrency, false);

        List<Coin> availableCoins = helper.getAvailableCoins(context, glMinBet, glMaxBet, targetCurrency);

        long minAllowedCoinValue = (long) Math.ceil(glMinBet / minCredits);
        assertThat(availableCoins, everyItem(hasProperty("value", greaterThanOrEqualTo(minAllowedCoinValue))));
    }

    @Test
    public void testGetAvailableCoinsFilterByMaxBetWithMultiplier() throws CommonException {
        double glMaxBet = 5000.0;
        double glMinBet = 0.0;
        String targetCurrency = "EUR";
        int maxCredits = 20;
        int currencyMultiplier = 100;
        when(template.getMaxCredits()).thenReturn(maxCredits);
        GamesLevelContext context = getContext(Collections.emptyMap(), "EUR", "USD", true, targetCurrency, false, currencyMultiplier);

        List<Coin> availableCoins = helper.getAvailableCoins(context, glMinBet, glMaxBet, targetCurrency);

        long maxAllowedCoinValue = (long) Math.floor(glMaxBet / maxCredits / currencyMultiplier);
        assertFalse(availableCoins.isEmpty());
        assertThat(availableCoins, everyItem(hasProperty("value", lessThanOrEqualTo(maxAllowedCoinValue))));
    }

    @Test
    public void testGetAvailableCoinsFilterByMaxBetIgnoreMultiplier() throws CommonException {
        double glMaxBet = 400.0;
        double glMinBet = 0.0;
        String targetCurrency = "EUR";
        int maxCredits = 20;
        int currencyMultiplier = 100;
        when(template.getMaxCredits()).thenReturn(maxCredits);
        Map<String, String> gameProperties = new HashMap<>();
        gameProperties.put(BaseGameConstants.KEY_GL_MAX_BET, "400");
        GamesLevelContext context = getContext(gameProperties, "EUR", "USD", true, targetCurrency, false, currencyMultiplier);

        List<Coin> availableCoins = helper.getAvailableCoins(context, glMinBet, glMaxBet, targetCurrency);

        long maxAllowedCoinValue = (long) Math.floor(glMaxBet / maxCredits);
        assertFalse(availableCoins.isEmpty());
        assertThat(availableCoins, everyItem(hasProperty("value", lessThanOrEqualTo(maxAllowedCoinValue))));
    }

    @Test
    public void testGetAvailableCoinsFilterByMinBetWithMultiplier() throws CommonException {
        double glMaxBet = 30000;
        double glMinBet = 5000;
        int maxCredits = 20;
        int minCredits = 5;
        int currencyMultiplier = 100;
        when(template.getMaxCredits()).thenReturn(maxCredits);
        when(template.getMinCredits()).thenReturn(minCredits);
        String targetCurrency = "EUR";
        GamesLevelContext context = getContext(Collections.emptyMap(), "EUR", "USD", true, targetCurrency, false, currencyMultiplier);

        List<Coin> availableCoins = helper.getAvailableCoins(context, glMinBet, glMaxBet, targetCurrency);

        long minAllowedCoinValue = (long) Math.ceil(glMinBet / minCredits / currencyMultiplier);
        assertThat(availableCoins, everyItem(hasProperty("value", greaterThanOrEqualTo(minAllowedCoinValue))));
    }

    @Test
    public void testGetAvailableCoinsFilterCoinByNominal() throws CommonException {
        double glMaxBet = Double.MAX_VALUE;
        double glMinBet = 0;
        when(template.getMaxCredits()).thenReturn(1);
        when(template.getMinCredits()).thenReturn(1);
        String targetCurrency = "EUR";
        GamesLevelContext context = getContext(Collections.emptyMap(), "EUR", "USD", true, targetCurrency, false);

        List<Coin> availableCoins = helper.getAvailableCoins(context, glMinBet, glMaxBet, targetCurrency);
        Predicate<Coin> coinsLargerTenIsMultipleOfFive = coin -> {
            boolean isMultipleOfFive = String.valueOf(coin.getValue()).endsWith("5") || String.valueOf(coin.getValue()).endsWith("0");
            return coin.getValue() <= 10 || isMultipleOfFive;
        };
        assertThat("All coins with nominal larger than 10 must be multiple of 5", Iterables.all(availableCoins, coinsLargerTenIsMultipleOfFive::test), is(true));
    }

    @Test
    public void testGetAvailableCoinsFilterForbiddenCoins() throws CommonException {
        double glMaxBet = Double.MAX_VALUE;
        double glMinBet = 0;
        when(template.getMaxCredits()).thenReturn(1);
        when(template.getMinCredits()).thenReturn(1);
        String targetCurrency = "EUR";
        GamesLevelContext context = getContext(Collections.emptyMap(), "EUR", "USD", true, targetCurrency, false);

        List<Coin> availableCoins = helper.getAvailableCoins(context, glMinBet, glMaxBet, targetCurrency);

        assertThat(availableCoins, everyItem(hasProperty("id", not(isIn(forbiddenCoins)))));
    }

    @Test
    public void testFilterCoinsByNumberAtLeastOneCoin() {
        List<Coin> actualCoins = helper.filterCoinsByNumber(Collections.emptyList(), 10);

        assertThat(actualCoins, hasSize(1));
        assertThat(actualCoins, everyItem(is(Coin.getByValue(1))));
    }

    @Test
    public void testFilterCoinsByNumberAvailableLessOrEqualCoinsNumber() {
        List<Coin> expectedCoins = Arrays.asList(
                Coin.getById(1),
                Coin.getById(2),
                Coin.getById(3),
                Coin.getById(4),
                Coin.getById(5)
        );

        List<Coin> actualCoins = helper.filterCoinsByNumber(expectedCoins, 10);

        assertThat(actualCoins, containsInAnyOrder(expectedCoins.toArray()));
    }

    @Test
    public void testFilterCoinsByNumberAvailableCoinsLargeThanNumberCoinsAndNumberCoinsLessThanThree() {
        List<Coin> coins = Arrays.asList(
                Coin.getById(1),
                Coin.getById(2),
                Coin.getById(3),
                Coin.getById(4),
                Coin.getById(5)
        );
        List<Coin> expectedCoins = Arrays.asList(
                Coin.getById(1),
                Coin.getById(2)
        );

        List<Coin> actualCoins = helper.filterCoinsByNumber(coins, 2);

        assertThat(actualCoins, containsInAnyOrder(expectedCoins.toArray()));
    }

    @Test
    public void testFilterCoinsByNumberThinOut() {
        List<Coin> coins = Arrays.asList(
                Coin.getById(1),
                Coin.getById(2),
                Coin.getById(3),
                Coin.getById(4),
                Coin.getById(5),
                Coin.getById(6),
                Coin.getById(7),
                Coin.getById(8),
                Coin.getById(9),
                Coin.getById(10)
        );
        List<Coin> expectedCoins = Arrays.asList(
                Coin.getById(1),
                Coin.getById(2),
                Coin.getById(4),
                Coin.getById(6),
                Coin.getById(8),
                Coin.getById(10)
        );

        List<Coin> actualCoins = helper.filterCoinsByNumber(coins, 6);

        assertThat(actualCoins, containsInAnyOrder(expectedCoins.toArray()));
    }

    private GamesLevelContext getContext(Map<String, String> gameProperties, String gameCurrency,
                                         String bankDefaultCurrency, boolean isGLUseDefaultCurrency,
                                         String targetCurrency, boolean needToConvert) {
        return getContext(gameProperties, gameCurrency, bankDefaultCurrency, isGLUseDefaultCurrency,
                targetCurrency, needToConvert, 1);
    }

    private GamesLevelContext getContext(Map<String, String> gameProperties, String gameCurrency,
                                         String bankDefaultCurrency, boolean isGLUseDefaultCurrency,
                                         String targetCurrency, boolean needToConvert, int currencyMultiplier) {
        IBaseGameInfo gameInfo = mock(IBaseGameInfo.class, RETURNS_DEEP_STUBS);
        when(gameInfo.getProperties()).thenReturn(gameProperties);
        when(gameInfo.getCurrency().getCode()).thenReturn(gameCurrency);
        when(bankInfo.isGLUseDefaultCurrency()).thenReturn(isGLUseDefaultCurrency);
        when(bankInfo.getDefaultCurrency().getCode()).thenReturn(bankDefaultCurrency);
        return new GamesLevelContext(template, gameInfo, bankInfo, targetCurrency, (s1, s2) -> needToConvert, currencyMultiplier);
    }
}