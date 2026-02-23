package com.dgphoenix.casino.gs.managers.game.settings;

import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:dader@dgphoenix.com">Timur Shaymardanov</a>
 * @since 23.09.2020
 */
public class GamesLevelHelper {

    private static final int DEFAULT_COINS_NUMBER = 11;
    private static final int LEGACY_CURRENCY_MINOR_UNIT_SCALE = 2;
    private static final double ONE_HUNDRED_CENTS = 100d;
    // Wave 3 scaffold: disabled by default, used only for safe parity assertion before any precision behavior switch.
    private static final String PRECISION_DUAL_CALC_COMPARE_PROPERTY = "abs.gs.phase8.precision.dualCalc.compare";
    private static final MathContext DIVIDE_CONTEXT = new MathContext(5, RoundingMode.DOWN);
    protected static final List<Long> forbiddenCoins = Arrays.asList(112L, 70L, 99L, 100L, 101L);

    private final ICurrencyRateManager currencyConverter;

    public GamesLevelHelper(ICurrencyRateManager currencyConverter) {
        this.currencyConverter = currencyConverter;
    }

    public double getGLMinBet(GamesLevelContext ctx) throws CommonException {
        //default 1 'cent' value from NBSF's Appendix
        double glMinBet = ctx.getTemplateMinCredits();
        Long templateMinBet = ctx.getTemplateMinBet();
        if (templateMinBet != null) {
            glMinBet = ctx.isNeedToConvert()
                    ? currencyConverter.convert(templateMinBet.doubleValue(), ICurrencyRateManager.DEFAULT_CURRENCY, ctx.getGlCurrency())
                    : templateMinBet.doubleValue();
        }
        Long bankMinBet = ctx.getBankMinBet();
        if (bankMinBet != null) {
            glMinBet = ctx.isNeedToConvert()
                    ? currencyConverter.convert(bankMinBet.doubleValue(), ctx.getBankCurrency(), ctx.getGlCurrency())
                    : bankMinBet.doubleValue();
        }
        Long gameMinBet = ctx.getGameMinBet();
        if (gameMinBet != null) {
            glMinBet = ctx.isNeedToConvert()
                    ? currencyConverter.convert(gameMinBet.doubleValue(), ctx.getGameCurrency(), ctx.getGlCurrency())
                    : gameMinBet.doubleValue();
        }
        return glMinBet;
    }

    public double getGLMaxBet(GamesLevelContext ctx) throws CommonException {
        // Legacy scale=2 default max-bet multiplier from NBSF's Appendix.
        verifyLegacyTemplateMaxBetParityForScale2IfEnabled(ctx.getTemplateMaxCredits());
        double glMaxBet = getLegacyTemplateMaxBet(ctx.getTemplateMaxCredits());
        Long templateMaxBet = ctx.getTemplateMaxBet();
        if (templateMaxBet != null) {
            glMaxBet = ctx.isNeedToConvert()
                    ? currencyConverter.convert(templateMaxBet.doubleValue(), ICurrencyRateManager.DEFAULT_CURRENCY, ctx.getGlCurrency())
                    : templateMaxBet.doubleValue();
        }
        Long bankMaxBet = ctx.getBankMaxBet();
        if (bankMaxBet != null) {
            glMaxBet = ctx.isNeedToConvert()
                    ? currencyConverter.convert(bankMaxBet.doubleValue(), ctx.getBankCurrency(), ctx.getGlCurrency())
                    : bankMaxBet.doubleValue();
        }
        Long gameMaxBet = ctx.getGameMaxBet();
        if (gameMaxBet != null) {
            glMaxBet = ctx.isNeedToConvert()
                    ? currencyConverter.convert(gameMaxBet.doubleValue(), ctx.getGameCurrency(), ctx.getGlCurrency())
                    : gameMaxBet.doubleValue();
        }
        return glMaxBet;
    }

    protected double getLegacyTemplateMaxBet(double templateMaxCredits) {
        return getTemplateMaxBetByMinorUnitScale(templateMaxCredits, LEGACY_CURRENCY_MINOR_UNIT_SCALE);
    }

    protected void verifyLegacyTemplateMaxBetParityForScale2IfEnabled(double templateMaxCredits) {
        if (!isPrecisionDualCalcComparisonEnabled()) {
            return;
        }
        double legacy = getLegacyTemplateMaxBet(templateMaxCredits);
        double generalized = getScaleReadyTemplateMaxBet(templateMaxCredits, LEGACY_CURRENCY_MINOR_UNIT_SCALE);
        if (Double.compare(legacy, generalized) != 0) {
            throw new IllegalStateException("Phase8 precision parity mismatch for template max bet: legacy="
                    + legacy + ", generalized=" + generalized + ", templateMaxCredits=" + templateMaxCredits);
        }
    }

    protected boolean isPrecisionDualCalcComparisonEnabled() {
        return Boolean.getBoolean(PRECISION_DUAL_CALC_COMPARE_PROPERTY);
    }

    protected double getScaleReadyTemplateMaxBet(double templateMaxCredits, int minorUnitScale) {
        return getTemplateMaxBetByMinorUnitScale(templateMaxCredits, minorUnitScale);
    }

    protected double getTemplateMaxBetByMinorUnitScale(double templateMaxCredits, int minorUnitScale) {
        return getMinorUnitMultiplierByScale(minorUnitScale) * templateMaxCredits;
    }

    protected double getMinorUnitMultiplierByScale(int minorUnitScale) {
        if (minorUnitScale == LEGACY_CURRENCY_MINOR_UNIT_SCALE) {
            return ONE_HUNDRED_CENTS;
        }
        if (minorUnitScale < 0 || minorUnitScale > 9) {
            throw new IllegalArgumentException("Unsupported minorUnitScale: " + minorUnitScale);
        }
        return Math.pow(10d, minorUnitScale);
    }

    public double limitGLMaxBetByAllowedMaxWin(GamesLevelContext ctx, double glMaxBet) throws CommonException {
        Double allowedMaxWin = null;
        Long bankMaxWin = ctx.getBankMaxWin();
        if (bankMaxWin != null) {
            allowedMaxWin = ctx.isNeedToConvert()
                    ? currencyConverter.convert(bankMaxWin.doubleValue(), ctx.getBankCurrency(), ctx.getGlCurrency())
                    : bankMaxWin.doubleValue();
        }
        Long gameMaxWin = ctx.getGameMaxWin();
        if (gameMaxWin != null) {
            allowedMaxWin = ctx.isNeedToConvert()
                    ? currencyConverter.convert(gameMaxWin.doubleValue(), ctx.getGameCurrency(), ctx.getGlCurrency())
                    : gameMaxWin.doubleValue();
        }
        if (allowedMaxWin != null) {
            String mathMaxWinString = ctx.getMaxWinString();
            if (!StringUtils.isTrimmedEmpty(mathMaxWinString)) {
                double allowedMaxBet = (allowedMaxWin / Integer.parseInt(mathMaxWinString)) * ctx.getTemplateMaxCredits();
                glMaxBet = Math.min(glMaxBet, allowedMaxBet);
            }
        }
        return glMaxBet;
    }

    public int getGLCoinsNumber(GamesLevelContext ctx) {
        //default coins number from NBSF's Appendix: 11
        int coinsNumber = ctx.getBankCoinsNumber() != null ? ctx.getBankCoinsNumber() : DEFAULT_COINS_NUMBER;
        Integer gameCoinsNumber = ctx.getGameCoinsNumber();
        if (gameCoinsNumber != null) {
            coinsNumber = gameCoinsNumber;
        }
        return coinsNumber;
    }

    public List<Coin> getAvailableCoins(GamesLevelContext ctx, double glMinBet, double glMaxBet, String targetCurrency) throws CommonException {
        List<Coin> availableCoins = Coin.getAll();
        if (ctx.isNeedToAdjustLimits()) {
            glMinBet = glMinBet / ctx.getCurrencyMultiplier();
            glMaxBet = glMaxBet / ctx.getCurrencyMultiplier();
        }
        double finalMaxBet = ctx.isNeedToConvert() ? currencyConverter.convert(glMaxBet, ctx.getGlCurrency(), targetCurrency) : glMaxBet;
        double finalMinBet = ctx.isNeedToConvert() ? currencyConverter.convert(glMinBet, ctx.getGlCurrency(), targetCurrency) : glMinBet;
        availableCoins.removeIf(coin -> coin.getValue() * ctx.getTemplateMaxCredits() > finalMaxBet);
        availableCoins.removeIf(coin -> coin.getValue() * ctx.getTemplateMinCredits() < finalMinBet);
        availableCoins.removeIf(coin -> coin.getValue() > 10 && coin.getValue() % 5 != 0);
        availableCoins.removeIf(coin -> forbiddenCoins.contains(coin.getId()));
        return availableCoins;
    }

    public List<Coin> filterCoinsByNumber(List<Coin> availableCoins, int coinsNumber) {
        List<Coin> resultCoins = new ArrayList<>();
        if (availableCoins.isEmpty()) {
            resultCoins.add(Coin.getByValue(1));
        } else if (availableCoins.size() <= coinsNumber) {
            resultCoins = availableCoins;
        } else if (coinsNumber < 3) {
            resultCoins = availableCoins.subList(0, coinsNumber);
        } else {
            resultCoins.add(availableCoins.get(0));
            BigDecimal inclusionFrequency = new BigDecimal(availableCoins.size())
                    .divide(new BigDecimal(coinsNumber - 1), DIVIDE_CONTEXT);
            BigDecimal inclusionThreshold = inclusionFrequency.subtract(new BigDecimal(1));
            for (int i = 1; i < availableCoins.size(); i++) {
                BigDecimal position = new BigDecimal(i);
                if (position.compareTo(inclusionThreshold) >= 0) {
                    Coin coin = availableCoins.get(i);
                    resultCoins.add(coin);
                    inclusionThreshold = inclusionThreshold.add(inclusionFrequency);
                }
            }
        }
        return resultCoins;
    }

}
