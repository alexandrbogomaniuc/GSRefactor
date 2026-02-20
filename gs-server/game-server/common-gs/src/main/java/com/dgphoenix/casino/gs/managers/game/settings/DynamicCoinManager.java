package com.dgphoenix.casino.gs.managers.game.settings;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.currency.ICurrency;
import com.dgphoenix.casino.common.cache.data.game.BaseGameConstants;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.game.GameVariableType;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.property.PropertyUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * @author <a href="mailto:dader@dgphoenix.com">Timur Shaymardanov</a>
 * @since 24.09.2020
 */
public class DynamicCoinManager {

    private final BankInfoCache bankInfoCache;
    private final BaseGameInfoTemplateCache baseGameInfoTemplateCache;
    private final ICurrencyRateManager currencyConverter;
    private final GamesLevelHelper gamesLevelHelper;

    public DynamicCoinManager(BankInfoCache bankInfoCache, BaseGameInfoTemplateCache baseGameInfoTemplateCache,
                              ICurrencyRateManager currencyConverter, GamesLevelHelper gamesLevelHelper) {
        this.bankInfoCache = bankInfoCache;
        this.baseGameInfoTemplateCache = baseGameInfoTemplateCache;
        this.currencyConverter = currencyConverter;
        this.gamesLevelHelper = gamesLevelHelper;
    }

    public Optional<Integer> getDynamicDefaultCoin(IBaseGameInfo gameInfo, String targetCurrency, double[] coinseq,
                                                   BiPredicate<String, String> currencyConversionChecker) throws CommonException {
        BankInfo bankInfo = bankInfoCache.getBankInfo(gameInfo.getBankId());
        Long defaultBet = null;
        String sourceCurrency = bankInfo.isGLUseDefaultCurrency() ? bankInfo.getDefaultCurrency().getCode() : ICurrencyRateManager.DEFAULT_CURRENCY;
        if (bankInfo.getDefaultBet() != null) {
            defaultBet = bankInfo.getDefaultBet();
        }
        Long gameDefaultBet = PropertyUtils.getLongProperty(gameInfo.getProperties(), BaseGameConstants.KEY_GL_DEFAULT_BET);
        if (gameDefaultBet != null) {
            defaultBet = gameDefaultBet;
            sourceCurrency = gameInfo.getCurrency().getCode();
        }

        if (defaultBet != null && coinseq.length >= 2) {
            boolean needToConvert = currencyConversionChecker.test(sourceCurrency, targetCurrency);
            double finalDefaultBet = needToConvert
                    ? currencyConverter.convert(defaultBet, sourceCurrency, targetCurrency)
                    : defaultBet;
            int baseBetInCurrency = Integer.parseInt(gameInfo.getProperty(BaseGameConstants.KEY_DEFAULTNUMLINES)) * 100;
            int coinIndex = 0;
            double delta = 0;
            for (int i = 0; i < coinseq.length; i++) {
                double currentDelta = Math.abs(coinseq[i] * baseBetInCurrency - finalDefaultBet);
                if (currentDelta < delta || i == 0) {
                    delta = currentDelta;
                    coinIndex = i;
                }
            }
            return Optional.of(coinIndex);
        }
        return Optional.empty();
    }

    public Optional<Long> getDynamicFrbCoin(IBaseGameInfo gameInfo, ICurrency targetCurrency,
                                            BiPredicate<String, String> currencyConversionChecker) throws CommonException {
        Long frbModeCredits = getFrbModeCredits(gameInfo);
        if (frbModeCredits == null) {
            return Optional.empty();
        }
        BankInfo bankInfo = bankInfoCache.getBankInfo(gameInfo.getBankId());
        String sourceCurrency = bankInfo.isGLUseDefaultCurrency()
                ? bankInfo.getDefaultCurrency().getCode() : ICurrencyRateManager.DEFAULT_CURRENCY;
        boolean needToConvert = currencyConversionChecker.test(sourceCurrency, targetCurrency.getCode());
        double frbBetValue = needToConvert
                ? currencyConverter.convert(bankInfo.getFRBBet(), sourceCurrency, targetCurrency.getCode())
                : bankInfo.getFRBBet();
        return Coin.getAll().stream()
                .filter(coin -> !GamesLevelHelper.forbiddenCoins.contains(coin.getId()))
                .filter(coin -> !(coin.getValue() > 10 && coin.getValue() % 5 != 0))
                .filter(coin -> coin.getValue() * frbModeCredits <= frbBetValue)
                .map(Coin::getValue)
                .max(Comparator.naturalOrder());
    }

    public Long getFrbModeCredits(IBaseGameInfo gameInfo) {
        if (gameInfo.getVariableType().equals(GameVariableType.COIN)) {
            String dedicatedFrbLines = gameInfo.getProperty(BaseGameConstants.KEY_FRB_DEFAULTNUMLINES);
            if (!StringUtils.isTrimmedEmpty(dedicatedFrbLines)) {
                return Long.parseLong(dedicatedFrbLines) * getFrbModeBpl(gameInfo);
            }
            String defaultLines = gameInfo.getProperty(BaseGameConstants.KEY_DEFAULTNUMLINES);
            if (!StringUtils.isTrimmedEmpty(defaultLines)) {
                return Long.parseLong(defaultLines) * getFrbModeBpl(gameInfo);
            }
        }
        return null;
    }

    private long getFrbModeBpl(IBaseGameInfo gameInfo) {
        String dedicatedFrbBpl = gameInfo.getProperty(BaseGameConstants.KEY_FRB_DEFAULTBETPERLINE);
        if (!StringUtils.isTrimmedEmpty(dedicatedFrbBpl)) {
            return Long.parseLong(dedicatedFrbBpl);
        }
        String defaultBpl = gameInfo.getProperty(BaseGameConstants.KEY_DEFAULTBETPERLINE);
        if (!StringUtils.isTrimmedEmpty(defaultBpl)) {
            return Long.parseLong(defaultBpl);
        }
        return 1L;
    }

    public List<Coin> getDynamicCoins(IBaseGameInfo gameInfo, String targetCurrency, BiPredicate<String, String> currencyConversionChecker) throws CommonException {
        long now = System.currentTimeMillis();
        BankInfo bankInfo = bankInfoCache.getBankInfo(gameInfo.getBankId());
        BaseGameInfoTemplate template = baseGameInfoTemplateCache.getBaseGameInfoTemplateById(gameInfo.getId());
        int currencyMultiplier = bankInfoCache.getCurrencyRateMultiplier(bankInfo.getId(), targetCurrency);
        GamesLevelContext gamesLevelContext = new GamesLevelContext(template, gameInfo, bankInfo, targetCurrency,
                currencyConversionChecker, currencyMultiplier);

        double glMinBet = gamesLevelHelper.getGLMinBet(gamesLevelContext);
        double glMaxBet = gamesLevelHelper.getGLMaxBet(gamesLevelContext);
        glMaxBet = gamesLevelHelper.limitGLMaxBetByAllowedMaxWin(gamesLevelContext, glMaxBet);
        int coinsNumber = gamesLevelHelper.getGLCoinsNumber(gamesLevelContext);
        List<Coin> availableCoins = gamesLevelHelper.getAvailableCoins(gamesLevelContext, glMinBet, glMaxBet, targetCurrency);
        List<Coin> resultCoins = gamesLevelHelper.filterCoinsByNumber(availableCoins, coinsNumber);

        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                " getDynamicCoins", System.currentTimeMillis() - now);

        return resultCoins;
    }

}
