package com.abs.casino.gs.managers.game.settings;

import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.currency.ICurrency;
import com.dgphoenix.casino.common.cache.data.game.BaseGameConstants;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfo;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.gs.managers.dblink.DBLink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * @author <a href="mailto:dader@dgphoenix.com">Timur Shaymardanov</a>
 * @since 18.09.2020
 */
@SuppressWarnings("rawtypes")
public class GameSettingsManager {

    private static final Logger LOG = LogManager.getLogger(GameSettingsManager.class);

    private final ICurrencyRateManager currencyConverter;
    private final BaseGameInfoTemplateCache baseGameInfoTemplateCache;
    private final SessionHelper sessionHelper;
    private final DynamicCoinManager dynamicCoinManager;

    public GameSettingsManager(ICurrencyRateManager currencyConverter,
                               BaseGameInfoTemplateCache baseGameInfoTemplateCache,
                               SessionHelper sessionHelper,
                               DynamicCoinManager dynamicCoinManager) {
        this.currencyConverter = currencyConverter;
        this.baseGameInfoTemplateCache = baseGameInfoTemplateCache;
        this.sessionHelper = sessionHelper;
        this.dynamicCoinManager = dynamicCoinManager;
    }

    @Nullable
    public Integer getDefaultCoin(@Nonnull DBLink dbLink) {
        IBaseGameInfo gameInfo = dbLink.getGameSettings();
        String targetCurrency = dbLink.getCurrency().getCode();
        Integer defaultCoin = gameInfo.getDefaultCoin();
        if (isDynamicLevelsSupported(gameInfo)) {
            return getDynamicDefaultCoin(gameInfo, targetCurrency, dbLink.getCOINSEQ(), dbLink::logError)
                    .orElse(defaultCoin);
        }
        return defaultCoin;
    }

    @Nullable
    public Integer getDefaultCoin(@Nonnull IBaseGameInfo gameInfo, @Nonnull String targetCurrency) {
        Integer defaultCoin = gameInfo.getDefaultCoin();
        if (isDynamicLevelsSupported(gameInfo)) {
            List<Coin> coins = getCoins(gameInfo, targetCurrency, false);
            double[] coinSequence = coinsToSequence(coins);
            return getDynamicDefaultCoin(gameInfo, targetCurrency, coinSequence, LOG::error)
                    .orElse(defaultCoin);
        }
        return defaultCoin;
    }

    @Nullable
    public Integer getDefaultCoin(@Nonnull IBaseGameInfo gameInfo, @Nonnull String targetCurrency, @Nonnull List<Coin> dynamicCoins) {
        Integer defaultCoin = gameInfo.getDefaultCoin();
        if (isDynamicLevelsSupported(gameInfo)) {
            double[] coinSequence = coinsToSequence(dynamicCoins);
            return getDynamicDefaultCoin(gameInfo, targetCurrency, coinSequence, LOG::error)
                    .orElse(defaultCoin);
        }
        return defaultCoin;
    }

    private Optional<Integer> getDynamicDefaultCoin(IBaseGameInfo gameInfo, String targetCurrency, double[] coinSequence, BiConsumer<String, Throwable> logger) {
        try {
            BiPredicate<String, String> conversionChecker = currencyConversionChecker(logger);
            return dynamicCoinManager.getDynamicDefaultCoin(gameInfo, targetCurrency, coinSequence, conversionChecker);
        } catch (Exception e) {
            logger.accept("Unable to calculate Default Coin dynamically, using an original value", e);
            return Optional.empty();
        }
    }

    @Nullable
    public String getFRBCoin(@Nonnull BankInfo bankInfo, @Nonnull DBLink dbLink) {
        IBaseGameInfo gameInfo = dbLink.getGameSettings();
        return getFRBCoin(bankInfo, gameInfo, dbLink.getCurrency());
    }

    @Nullable
    public String getFRBCoin(@Nonnull BankInfo bankInfo, @Nonnull IBaseGameInfo gameInfo, @Nonnull ICurrency targetCurrency) {
        String frbCoin = gameInfo.getProperty(BaseGameConstants.KEY_FRB_COIN);
        if (!bankInfo.isUseFRBBetForNonGLSlots() && !isDynamicLevelsSupported(gameInfo)) {
            return frbCoin;
        }
        if (bankInfo.getFRBBet() == null || isFRBCoinPredefined(bankInfo, gameInfo, targetCurrency)) {
            return frbCoin;
        }
        long targetCoin = getDynamicFrbCoin(gameInfo, targetCurrency, LOG::error).orElse(1L);
        return Long.toString(targetCoin);
    }

    @Nullable
    public Long getFrbModeCredits(IBaseGameInfo gameInfo) {
        return dynamicCoinManager.getFrbModeCredits(gameInfo);
    }

    private Optional<Long> getDynamicFrbCoin(IBaseGameInfo gameInfo, ICurrency targetCurrency, BiConsumer<String, Throwable> logger) {
        try {
            BiPredicate<String, String> conversionChecker = currencyConversionChecker(logger);
            return dynamicCoinManager.getDynamicFrbCoin(gameInfo, targetCurrency, conversionChecker);
        } catch (Exception e) {
            logger.accept("Unable to calculate FRB Coin dynamically, using the lowest possible: 0.01", e);
            return Optional.empty();
        }
    }

    private boolean isFRBCoinPredefined(BankInfo bankInfo, IBaseGameInfo gameInfo, ICurrency currency) {
        if (bankInfo.isStaticFrbCoinsIgnored()) {
            return false;
        }
        String frbCoin = gameInfo.getProperty(BaseGameConstants.KEY_FRB_COIN);
        if (StringUtils.isTrimmedEmpty(frbCoin)) {
            return false;
        }
        IBaseGameInfo sharedInfo = BaseGameCache.getInstance().getGameInfoShared(bankInfo.getId(), gameInfo.getId(), currency);
        if (sharedInfo == null) {
            return false;
        }
        BaseGameInfo defaultGameInfo = baseGameInfoTemplateCache.getDefaultGameInfo(gameInfo.getId());
        String defaultFrbCoin = defaultGameInfo.getProperty(BaseGameConstants.KEY_FRB_COIN);
        if (StringUtils.isTrimmedEmpty(defaultFrbCoin)) {
            return true;
        } else {
            return !defaultFrbCoin.equals(frbCoin);
        }
    }

    public List<Coin> getCoins(@Nonnull DBLink dbLink) {
        return getCoins(dbLink,true);
    }

    public List<Coin> getCoins(@Nonnull DBLink dbLink, boolean withCache) {
        return getCoins(dbLink.getGameSettings(), dbLink.getCurrency().getCode(), dbLink::logError, withCache);
    }

    public List<Coin> getCoins(@Nonnull IBaseGameInfo gameInfo, @Nonnull String targetCurrency, boolean withCache) {
        return getCoins(gameInfo, targetCurrency, LOG::error, withCache);
    }

    private List<Coin> getCoins(IBaseGameInfo gameInfo, String targetCurrency, BiConsumer<String, Throwable> logger, boolean withCache) {
        List<Coin> coins = prepareCoins(gameInfo, targetCurrency, logger, withCache);
        coins = filterByMaxCoinLimit(coins, gameInfo, targetCurrency);
        return coins;
    }

    private List<Coin> prepareCoins(IBaseGameInfo gameInfo, String targetCurrency, BiConsumer<String, Throwable> logger, boolean withCache) {
        List<Coin> coins;
        if (isDynamicLevelsSupported(gameInfo)) {
            coins = withCache ? prepareCoinsDynamicallyWithCaching(gameInfo, targetCurrency, logger) : prepareCoinsDynamically(gameInfo, targetCurrency, logger);
        } else {
            coins = withCache ? prepareCoinsWithCaching(gameInfo) : gameInfo.getCoins();
        }
        return coins;
    }

    private List<Coin> prepareCoinsDynamicallyWithCaching(IBaseGameInfo gameInfo, String targetCurrency, BiConsumer<String, Throwable> logger) {
        List<Coin> coins = getGameSession().getCachedDynamicCoins();
        if (coins == null) {
            coins = prepareCoinsDynamically(gameInfo, targetCurrency, logger);
            getGameSession().cacheDynamicCoins(coins);
        }
        return coins;
    }

    private List<Coin> prepareCoinsWithCaching(IBaseGameInfo gameInfo) {
        List<Coin> coins = getGameSession().getCachedDynamicCoins();
        if (coins == null) {
            coins = new ArrayList<>(gameInfo.getCoins());
            getGameSession().cacheDynamicCoins(coins);
        }
        return coins;
    }

    private List<Coin> filterByMaxCoinLimit(List<Coin> coins, IBaseGameInfo gameInfo, String targetCurrency) {
        List<Coin> resultCoins = new ArrayList<>(coins);
        try {
            String maxCoinLimitEurCentsStr = gameInfo.getProperty(BaseGameConstants.KEY_MAX_COIN_LIMIT_EUR);
            if (!StringUtils.isTrimmedEmpty(maxCoinLimitEurCentsStr)) {
                long maxCoinLimitEurCents = Long.parseLong(maxCoinLimitEurCentsStr);
                double rate = currencyConverter.getRateToBaseCurrency(targetCurrency);
                if (rate <= 0) {
                    LOG.warn("filterWithMaxCoinLimit::Rate={}, can't convert to EUR, currency={}", rate, targetCurrency);
                    return coins;
                }
                ArrayList<Coin> coinsEnd = new ArrayList<>();
                for (Coin coin : coins) {
                    double valueInEURCents = coin.getValue() * rate;
                    if (valueInEURCents <= maxCoinLimitEurCents) {
                        coinsEnd.add(coin);
                    } else {
                        LOG.warn("filterWithMaxCoinLimit: coin={}, not available, exceeded maxCoinLimitEurCents={}, valueInEURCents={}",
                                coin, maxCoinLimitEurCents, valueInEURCents);
                    }
                }
                return coinsEnd;
            }
        } catch (Exception e) {
            LOG.error("Can't calculate max coin limit", e);
        }
        return resultCoins;
    }

    private List<Coin> prepareCoinsDynamically(IBaseGameInfo gameInfo, String targetCurrency, BiConsumer<String, Throwable> logger) {
        List<Coin> coins = gameInfo.getCoins();
        try {
            return dynamicCoinManager.getDynamicCoins(gameInfo, targetCurrency, currencyConversionChecker(logger));
        } catch (Exception e) {
            logger.accept("Unable to prepare coins dynamically, using GameSettings values", e);
        }
        return coins;
    }

    private BiPredicate<String, String> currencyConversionChecker(BiConsumer<String, Throwable> logger) {
        return (sourceCurrency, targetCurrency) -> isConversionPossible(sourceCurrency, targetCurrency, logger);
    }

    private boolean isConversionPossible(String sourceCurrency, String targetCurrency, BiConsumer<String, Throwable> logger) {
        try {
            currencyConverter.convert(1, sourceCurrency, targetCurrency);
        } catch (CommonException e) {
            String message = "Conversion from " + sourceCurrency + " to " + targetCurrency
                    + " isn't possible, leaving original values; reason: " + e.getMessage();
            LOG.warn(message);
            return false;
        }
        return true;
    }

    private GameSession getGameSession() {
        return sessionHelper.getTransactionData().getGameSession();
    }

    private boolean isDynamicLevelsSupported(IBaseGameInfo gameInfo) {
        long gameId = gameInfo.getId();
        return baseGameInfoTemplateCache.getBaseGameInfoTemplateById(gameId).isDynamicLevelsSupported();
    }

    private double[] coinsToSequence(List<Coin> coins) {
        return coins.stream()
                .sequential()
                .map(Coin::getValue)
                .mapToDouble(Long::doubleValue)
                .map(value -> value / 100.0)
                .toArray();
    }
}
