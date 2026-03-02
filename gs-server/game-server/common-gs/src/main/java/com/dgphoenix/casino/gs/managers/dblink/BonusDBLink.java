package com.abs.casino.gs.managers.dblink;

import com.abs.casino.common.SessionHelper;
import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.BaseGameCache;
import com.abs.casino.common.cache.BaseGameInfoTemplateCache;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.account.LasthandInfo;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.bank.Coin;
import com.abs.casino.common.cache.data.bank.Limit;
import com.abs.casino.common.cache.data.bonus.Bonus;
import com.abs.casino.common.cache.data.bonus.BonusStatus;
import com.abs.casino.common.cache.data.bonus.BonusSystemType;
import com.abs.casino.common.cache.data.currency.Currency;
import com.abs.casino.common.cache.data.game.BaseGameConstants;
import com.abs.casino.common.cache.data.game.GameMode;
import com.abs.casino.common.cache.data.game.GameVariableType;
import com.abs.casino.common.cache.data.game.IBaseGameInfo;
import com.abs.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.currency.ICurrencyRateManager;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.logkit.GameLog;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.gs.managers.payment.bonus.BonusManager;
import com.abs.casino.gs.persistance.LasthandPersister;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class BonusDBLink extends DBLink {

    private Long bonusId;
    private BigDecimal rolloverPercent;
    private final boolean isBonusInstantLostOnThreshold;

    public BonusDBLink(long accountId, String nickName, long bankId, long gameId, String gameName,
                       boolean isJackpotGame, GameSession gameSession, Currency currency, Long bonusId)
            throws CommonException {
        super(accountId, nickName, bankId, gameId, gameName, isJackpotGame, gameSession, currency);
        this.bonusId = bonusId;
        setRoundId(getRoundIdFromLastHand());
        try {
            Bonus bonus = BonusManager.getInstance().getById(bonusId);
            if (bonus == null) {
                Bonus archivedBonus = BonusManager.getInstance().getArchivedBonusById(bonusId);
                if (archivedBonus == null) {
                    throw new CommonException("bonus not found: id=" + bonusId);
                }
                if (BonusStatus.ACTIVE.equals(archivedBonus.getStatus())) {
                    logError("BonusDBLink: Strange error, active bonus in archive=" + archivedBonus);
                    throw new CommonException("Strange error, active bonus in archive, " +
                            "bonus not found: id=" + bonusId);
                }
                logWarn("BonusDBLink: bonus moved to archive, make temporary BonusDBLink, " +
                        "archivedBonus=" + archivedBonus);
                bonus = archivedBonus;
            } else {
                SessionHelper.getInstance().getTransactionData().setBonus(bonus);
            }

            gameSession.setBonusStatus(bonus.getStatus());
            AccountInfo accountInfo = getAccount();
            IBaseGameInfo<?, ?> gameInfo = BaseGameCache.getInstance().getGameInfoById(accountInfo.getBankId(), gameSession.getGameId(),
                    accountInfo.getCurrencyFraction() == null ? accountInfo.getCurrency() : accountInfo.getCurrencyFraction());
            String rolloverPercentStr = gameInfo.getProperty(BaseGameConstants.KEY_ROLLOVER_PERCENT);
            if (!StringUtils.isTrimmedEmpty(rolloverPercentStr)) {
                rolloverPercent = new BigDecimal(rolloverPercentStr);
            }

            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
            isBonusInstantLostOnThreshold = bankInfo.isBonusInstantLostOnThreshold();
        } catch (Exception e) {
            logError("BonusDBLink init error:", e);
            throw new CommonException(e);
        }
    }

    public BonusDBLink(AccountInfo accountInfo, long gameId, Long gameSessionId, Long bonusId, SessionInfo sessionInfo,
                       IBaseGameInfo<?, ?> gameInfo, String lang) throws CommonException {
        super(accountInfo, gameId, gameSessionId, sessionInfo, gameInfo, lang);
        this.bonusId = bonusId;
        setRoundId(getRoundIdFromLastHand());
        try {
            Bonus bonus = BonusManager.getInstance().getById(bonusId);
            if (bonus == null) {
                Bonus archivedBonus = BonusManager.getInstance().getArchivedBonusById(bonusId);
                if (archivedBonus == null) {
                    throw new CommonException("bonus not found: id=" + bonusId);
                }
                if (BonusStatus.ACTIVE.equals(archivedBonus.getStatus())) {
                    logError("BonusDBLink: Strange error, active bonus in archive=" + archivedBonus);
                    throw new CommonException("Strange error, active bonus in archive, " +
                            "bonus not found: id=" + bonusId);
                }
                logWarn("BonusDBLink: bonus moved to archive, make temporary BonusDBLink, " +
                        "archivedBonus=" + archivedBonus);
                bonus = archivedBonus;
            } else {
                SessionHelper.getInstance().getTransactionData().setBonus(bonus);
            }

            GameSession gameSession = getGameSession();
            gameSession.setBonusId(bonusId);
            gameSession.setBonusStatus(bonus.getStatus());
            gameSession.setStartBonusBalance(bonus.getBalance());
            String rolloverPercentStr = gameInfo.getProperty(BaseGameConstants.KEY_ROLLOVER_PERCENT);
            if (!StringUtils.isTrimmedEmpty(rolloverPercentStr)) {
                rolloverPercent = new BigDecimal(rolloverPercentStr);
            }

            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
            isBonusInstantLostOnThreshold = bankInfo.isBonusInstantLostOnThreshold();
        } catch (Exception e) {
            logError("BonusDBLink init error:", e);
            throw new CommonException(e);
        }

    }

    @Override
    public List<Coin> getCoins() {
        List<Coin> originalCoins = super.getCoins();
        try {
            if (getGameSettings().getVariableType() == GameVariableType.COIN) {
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(getBankId());
                if (bankInfo.getOCBMaxBet() == null) {
                    return originalCoins;
                }
                double maxBetInPlayerCurrency = getMaxOCBBetInPlayerCurrency(bankInfo, bankInfo.getOCBMaxBet());
                int maxCredits = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId).getMaxCredits();
                if (maxCredits == 0) return originalCoins;

                Coin minCoin = null;
                ArrayList<Coin> limitedCoins = new ArrayList<>();
                for (Coin coin : originalCoins) {
                    double betValue = coin.getValue() * maxCredits;
                    if (betValue <= maxBetInPlayerCurrency) {
                        limitedCoins.add(coin);
                    } else {
                        logWarn("getCoins: coin=" + coin + ", not available, exceeded maxBetInPlayerCurrency=" +
                                maxBetInPlayerCurrency + ", betValue=" + betValue);
                    }
                    if (minCoin == null || coin.getValue() < minCoin.getValue()) minCoin = coin;
                }
                if (limitedCoins.isEmpty()) {
                    logWarn("Limited coins set is empty, adding min coin to proceed OCB with: " + minCoin);
                    limitedCoins.add(minCoin);
                }
                GameLog.getInstance().debug("limitedCoins: " + limitedCoins.toString());
                return limitedCoins;
            } else {
                return originalCoins;
            }
        } catch (Exception e) {
            logError("Can't calculate OCB coins set", e);
        }
        return originalCoins;
    }

    @Override
    public Limit getLimit() {
        Limit originalLimit = super.getLimit();
        try {
            if (getGameSettings().getVariableType() == GameVariableType.LIMIT) {
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(getBankId());
                if (bankInfo.getOCBMaxTableLimit() == null) {
                    return originalLimit;
                }
                double maxLimitInPlayerCurrency = getMaxOCBBetInPlayerCurrency(bankInfo, bankInfo.getOCBMaxTableLimit());
                if (originalLimit.getMaxValue() <= maxLimitInPlayerCurrency) {
                    GameLog.getInstance().debug("Using original limit: " + originalLimit.toString());
                    return originalLimit;
                } else if (originalLimit.getMinValue() < maxLimitInPlayerCurrency
                        && originalLimit.getMaxValue() > maxLimitInPlayerCurrency) {
                    Limit croppedLimit = Limit.valueOf(-1, originalLimit.getMinValue(), (int) maxLimitInPlayerCurrency);
                    GameLog.getInstance().debug("Using limit cropped on top: " + croppedLimit.toString());
                    return croppedLimit;
                } else {
                    Limit fromRegistered = null;
                    for (Limit limit : Limit.getAllRegistered()) {
                        if (limit.getMaxValue() < maxLimitInPlayerCurrency) {
                            if (fromRegistered == null ||
                                    maxLimitInPlayerCurrency - fromRegistered.getMaxValue() >
                                            maxLimitInPlayerCurrency - limit.getMaxValue()) {
                                fromRegistered = limit;
                            }
                        } else if (limit.getMaxValue() == maxLimitInPlayerCurrency) {
                            fromRegistered = limit;
                            break;
                        }
                    }
                    if (fromRegistered != null) {
                        GameLog.getInstance().debug("Using limit chosen from registered: " + fromRegistered.toString());
                        return fromRegistered;
                    } else {
                        GameLog.getInstance().debug("Using original limit as final possible: " + originalLimit.toString());
                        return originalLimit;
                    }
                }
            } else {
                return originalLimit;
            }
        } catch (Exception e) {
            logError("Can't choose OCB limit", e);
        }
        return originalLimit;
    }

    @Override
    public String getGameSettingsProperty(String key) {
        if (gameName.toUpperCase().contains("ROULETTE") && key.startsWith("MAX_BET_")) {
            try {
                double maxLimit = (getLimit().getMaxValue()) / 100.0;
                double result;
                int internalLimitIndex = Integer.parseInt(key.replace("MAX_BET_", ""));
                switch (internalLimitIndex) {
                    case 1:
                        result = maxLimit / 20.0;
                        break;
                    case 2:
                        result = maxLimit / 20.0;
                        break;
                    case 3:
                        result = maxLimit / 20.0;
                        break;
                    case 5:
                        result = maxLimit / 20.0;
                        break;
                    case 4:
                        result = maxLimit / 10.0;
                        break;
                    case 6:
                        result = maxLimit / 5.0;
                        break;
                    case 12:
                        result = maxLimit / 2.0;
                        break;
                    case 18:
                        result = maxLimit;
                        break;
                    default:
                        result = 0;
                        break;
                }
                String internalLimit = BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_DOWN).toString();
                GameLog.getInstance().debug("Using custom internal limit for " + key + ": " + internalLimit);
                return internalLimit;
            } catch (NumberFormatException e) {
                logError("Can't parse internal limit index: " + key, e);
                return super.getGameSettingsProperty(key);
            }
        }
        return super.getGameSettingsProperty(key);
    }

    private double getMaxOCBBetInPlayerCurrency(BankInfo bankInfo, Long maxBet) throws CommonException {
        return currencyConverter.convert(maxBet, bankInfo.isGLUseDefaultCurrency() ?
                        bankInfo.getDefaultCurrency().getCode() : ICurrencyRateManager.DEFAULT_CURRENCY,
                getCurrency().getCode());
    }

    @Override
    protected void saveLastHandOnClose(LasthandInfo lasthand) {
        LasthandPersister.getInstance().saveOnClose(accountId, gameId, bonusId, BonusSystemType.ORDINARY_SYSTEM, lasthand);
    }

    public Bonus getBonus() {
        return SessionHelper.getInstance().getTransactionData().getBonus();
    }

    public Long getBonusId() {
        return bonusId;
    }

    public void setBonusId(Long bonusId) {
        this.bonusId = bonusId;
    }

    @Override
    public boolean isBonusInstantLostOnThreshold() {
        return isBonusInstantLostOnThreshold;
    }

    @Override
    public GameMode getMode() {
        return GameMode.BONUS;
    }

    @Override
    public long getBalanceLong() {
        Bonus bonus = getBonus();
        if (bonus != null) return bonus.getBalance();

        return -1;
    }

    @Override
    public boolean isBonusGameSession() {
        return true;
    }

    /**
     * SETBETS
     */
    @Override
    public void incrementBalance(long bet, long win) throws CommonException {
        Bonus bonus = getBonus();
        if (bonus != null) {
            bonus.incrementBalance(bet, false); //bet is negative
            bonus.incrementBalance(win, false);
            if (bet < 0) {
                long rolloverContribution = -bet;

                if (rolloverPercent != null) {
                    BigDecimal rolloverFractionalPart = BigDecimal.valueOf(bonus.getRolloverFractionalPart());
                    BigDecimal contribution = rolloverPercent.multiply(BigDecimal.valueOf(-bet), MathContext.DECIMAL128)
                            .add(rolloverFractionalPart);
                    rolloverContribution = contribution.longValue();
                    rolloverFractionalPart = contribution.subtract(BigDecimal.valueOf(rolloverContribution));
                    bonus.setRolloverFractionalPart(rolloverFractionalPart.doubleValue());
                }
                bonus.incrementBetSum(rolloverContribution);
            }
        } else {
            logWarn("cannot incrementBalance, bonus is null. possible reason, bonus not active");
        }
    }

    @Override
    public void setRoundFinished() throws CommonException {
        super.setRoundFinished();
    }

    @Override
    public String getLogPrefix() {
        return "GS:BonusDBLink [accountId=" + accountId + ", nickName=" + nickName + ", gameId=" + gameId
                + ", gameSessionId=" + gameSessionId + ", bonusId=" + bonusId + "] ";
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("BonusDBLink");
        sb.append("[accountId=").append(accountId);
        sb.append(", nickName='").append(nickName).append('\'');
        sb.append(", bonusId=").append(bonusId);
        sb.append(", bankId=").append(getBankId());
        sb.append(", gameId=").append(gameId);
        sb.append(", gameName='").append(gameName).append('\'');
        sb.append(", gameSessionId=").append(gameSessionId);
        sb.append(", minBet=").append(getMinBet());
        sb.append(", maxBet=").append(getMaxBet());
        sb.append(", COINSEQ=").append(getCOINSEQ() == null ? "null" : "");
        for (int i = 0; getCOINSEQ() != null && i < getCOINSEQ().length; ++i)
            sb.append(i == 0 ? "" : ", ").append(getCOINSEQ()[i]);
        sb.append(", limitsChanged=").append(isLimitsChanged());
        sb.append(", roundId=").append(getRoundId());
        sb.append(", winAmount=").append(getWinAmount());
        sb.append(']');
        return sb.toString();
    }

}
