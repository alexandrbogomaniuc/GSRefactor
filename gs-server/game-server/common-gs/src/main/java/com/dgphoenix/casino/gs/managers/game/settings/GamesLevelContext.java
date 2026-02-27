package com.abs.casino.gs.managers.game.settings;

import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.BaseGameConstants;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.abs.casino.common.util.property.PropertyUtils;

import java.util.Map;
import java.util.function.BiPredicate;

/**
 * @author <a href="mailto:dader@dgphoenix.com">Timur Shaymardanov</a>
 * @since 25.09.2020
 */
class GamesLevelContext {
    private final BaseGameInfoTemplate template;
    private final BankInfo bankInfo;
    private final IBaseGameInfo gameInfo;
    private final String gameCurrency;
    private final String bankCurrency;
    private final String glCurrency;
    private final boolean needToConvert;
    private final int currencyMultiplier;
    private final boolean needToAdjustLimits;

    public GamesLevelContext(BaseGameInfoTemplate template,
                             IBaseGameInfo gameInfo,
                             BankInfo bankInfo,
                             String targetCurrency,
                             BiPredicate<String, String> conversionChecker,
                             int currencyMultiplier) {
        this.template = template;
        this.bankInfo = bankInfo;
        this.gameInfo = gameInfo;
        this.gameCurrency = gameInfo.getCurrency().getCode();
        this.bankCurrency = bankInfo.isGLUseDefaultCurrency() ?
                bankInfo.getDefaultCurrency().getCode() : ICurrencyRateManager.DEFAULT_CURRENCY;
        this.glCurrency = gameCurrency.equals(targetCurrency) ? targetCurrency : bankCurrency;
        this.needToConvert = conversionChecker.test(glCurrency, targetCurrency);
        this.currencyMultiplier = currencyMultiplier;
        this.needToAdjustLimits = currencyMultiplier != 1 && !isGameLimitsConfigured(targetCurrency);
    }

    private boolean isGameLimitsConfigured(String targetCurrency) {
        if (gameCurrency.equals(targetCurrency)) {
            return getGameMinBet() != null || getGameMaxBet() != null || getGameMaxWin() != null;
        } else {
            return false;
        }
    }

    public String getGameCurrency() {
        return gameCurrency;
    }

    public String getBankCurrency() {
        return bankCurrency;
    }

    public String getGlCurrency() {
        return glCurrency;
    }

    public boolean isNeedToConvert() {
        return needToConvert;
    }

    public int getCurrencyMultiplier() {
        return currencyMultiplier;
    }

    public boolean isNeedToAdjustLimits() {
        return needToAdjustLimits;
    }

    public double getTemplateMinCredits() {
        return template.getMinCredits();
    }

    public double getTemplateMaxCredits() {
        return template.getMaxCredits();
    }

    public Long getTemplateMinBet() {
        return template.getMinBet();
    }

    public Long getTemplateMaxBet() {
        return template.getMaxBet();
    }

    public Long getBankMinBet() {
        return bankInfo.getMinBet();
    }

    public Long getBankMaxBet() {
        return bankInfo.getMaxBet();
    }

    public Long getBankMaxWin() {
        return bankInfo.getMaxWin();
    }

    public Integer getBankCoinsNumber() {
        return bankInfo.getCoinsNumber();
    }

    public Long getGameMinBet() {
        return PropertyUtils.getLongProperty(getGameProperties(), BaseGameConstants.KEY_GL_MIN_BET);
    }

    public Long getGameMaxBet() {
        return PropertyUtils.getLongProperty(getGameProperties(), BaseGameConstants.KEY_GL_MAX_BET);
    }

    public Long getGameMaxWin() {
        return PropertyUtils.getLongProperty(getGameProperties(), BaseGameConstants.KEY_GL_MAX_EXPOSURE);
    }

    public String getMaxWinString() {
        return template.getDefaultGameInfo().getProperty(BaseGameConstants.KEY_MAX_WIN);
    }

    public Integer getGameCoinsNumber() {
        return PropertyUtils.getIntProperty(getGameProperties(), BaseGameConstants.KEY_GL_NUMBER_OF_COINS);
    }

    public Map<String, String> getGameProperties() {
        return gameInfo.getProperties();
    }

}
