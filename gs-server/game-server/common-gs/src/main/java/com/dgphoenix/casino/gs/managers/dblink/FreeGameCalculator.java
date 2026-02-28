package com.abs.casino.gs.managers.dblink;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.game.BaseGameConstants;
import com.dgphoenix.casino.common.cache.data.game.GameGroup;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.abs.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.game.settings.GameSettingsManager;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;

import static com.abs.casino.common.cache.data.game.BaseGameConstants.KEY_FREEBALANCE;
import static com.abs.casino.common.cache.data.game.BaseGameConstants.KEY_FREEBALANCE_MULTIPLIER;
import static com.dgphoenix.casino.common.config.GameServerConfigTemplate.DEFAULT_FREE_BALANCE;
import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

public class FreeGameCalculator {
    private static final Logger LOG = LogManager.getLogger(FreeGameCalculator.class);
    public static final long DEFAULT_MQ_STAKES_RESERVE = 100;

    private final GameServerConfiguration serverConfig;
    private final ICurrencyRateManager currencyConverter;
    private final GameSettingsManager gameSettingsManager;
    private final BankInfoCache bankInfoCache;
    private final BaseGameInfoTemplateCache gameInfoTemplateCache;

    public FreeGameCalculator(GameServerConfiguration serverConfig, ICurrencyRateManager currencyConverter,
                              GameSettingsManager gameSettingsManager, BankInfoCache bankInfoCache, BaseGameInfoTemplateCache gameInfoTemplateCache) {
        this.serverConfig = serverConfig;
        this.currencyConverter = currencyConverter;
        this.gameSettingsManager = gameSettingsManager;
        this.bankInfoCache = bankInfoCache;
        this.gameInfoTemplateCache = gameInfoTemplateCache;
    }

    public long calculateFreeBalance(IBaseGameInfo<?, ?> gameInfo, String currencyCode) {
        long freeBalance;
        String gameFreeBalance = gameInfo.getProperty(KEY_FREEBALANCE);
        if (!isTrimmedEmpty(gameFreeBalance)) {
            freeBalance = Long.parseLong(gameFreeBalance);
        } else {
            String gameFBMultiplier = gameInfo.getProperty(KEY_FREEBALANCE_MULTIPLIER);
            if (!isTrimmedEmpty(gameFBMultiplier)) {
                freeBalance = getFreeBalanceByMultiplier(gameInfo, Integer.valueOf(gameFBMultiplier), currencyCode);
            } else {
                freeBalance = getFreeBalance(gameInfo, currencyCode);
            }
        }
        return freeBalance;
    }

    private long getFreeBalance(IBaseGameInfo<?, ?> gameInfo, String currencyCode) {
        long freeBalance;
        BankInfo bankInfo = bankInfoCache.getBankInfo(gameInfo.getBankId());
        Long bankFreeBalance = bankInfo.getFreeBalance();
        if (bankFreeBalance != null) {
            freeBalance = bankFreeBalance;
        } else {
            Integer bankFBMultiplier = bankInfo.getFreeBalanceMultiplier();
            if (bankFBMultiplier != null) {
                freeBalance = getFreeBalanceByMultiplier(gameInfo, bankFBMultiplier, currencyCode);
            } else {
                Integer serverFBMultiplier = serverConfig.getFreeBalanceMultiplier();
                if (serverFBMultiplier != null) {
                    freeBalance = getFreeBalanceByMultiplier(gameInfo, serverFBMultiplier, currencyCode);
                } else if (serverConfig.isDropFreeBalance()) {
                    freeBalance = getStartFreeBalance(gameInfo, currencyCode);
                } else {
                    freeBalance = serverConfig.getFreeBalance();
                }
            }
        }
        return freeBalance;
    }

    private long getFreeBalanceByMultiplier(IBaseGameInfo<?, ?> gameInfo, Integer multiplier, String currencyCode) {
        long freeBalance;
        GameGroup gameGroup = gameInfo.getGroup();
        switch (gameGroup) {
            case TABLE:
                int maxLimit = gameInfo.getLimit().getMaxValue();
                freeBalance = getRoundBalance((long) maxLimit * multiplier);
                break;

            case ACTION_GAMES:
                long maxCoinValue = Collections.max(gameSettingsManager.getCoins(gameInfo, currencyCode, true))
                        .getValue();
                String reserve = gameInfo.getProperty(BaseGameConstants.KEY_MQ_STAKES_RESERVE);
                long stakesReserve;
                if (!isTrimmedEmpty(reserve)) {
                    stakesReserve = Long.parseLong(reserve);
                } else {
                    stakesReserve = DEFAULT_MQ_STAKES_RESERVE;
                    LOG.warn("Missing game option KEY_MQ_STAKES_RESERVE");
                }
                freeBalance = getRoundBalance(maxCoinValue * stakesReserve * multiplier);
                break;

            case VIDEOPOKER:
                maxCoinValue = Collections.max(gameSettingsManager.getCoins(gameInfo, currencyCode, true)).getValue();
                String maxBetInCredits = gameInfo.getProperty(BaseGameConstants.KEY_MAX_BET_IN_CREDITS);
                long maxCredits;
                if (!isTrimmedEmpty(maxBetInCredits)) {
                    maxCredits = Long.parseLong(maxBetInCredits);
                    freeBalance = getRoundBalance(maxCoinValue * maxCredits * multiplier);
                } else {
                    LOG.warn("Missing game option MAX_BET_IN_CREDITS ");
                    freeBalance = getFreeBalanceFromConfigsByPriority(gameInfo, currencyCode);
                }
                break;

            case SLOTS:
                maxCoinValue = Collections.max(gameSettingsManager
                        .getCoins(gameInfo, currencyCode, true)).getValue();
                maxCredits = gameInfoTemplateCache.getBaseGameInfoTemplateById(gameInfo.getId())
                        .getMaxCredits();
                freeBalance = getRoundBalance(maxCoinValue * maxCredits * multiplier);
                break;

            default:
                freeBalance = getFreeBalanceFromConfigsByPriority(gameInfo, currencyCode);
                LOG.warn("Unknown game group");
        }
        return freeBalance;
    }

    private long getFreeBalanceFromConfigsByPriority(IBaseGameInfo<?, ?> gameInfo, String currencyCode) {
        long freeBalance;
        String gameFreeBalance = gameInfo.getProperty(KEY_FREEBALANCE);
        if (!isTrimmedEmpty(gameFreeBalance)) {
            freeBalance = Long.parseLong(gameFreeBalance);
        } else {
            BankInfo bankInfo = bankInfoCache.getBankInfo(gameInfo.getBankId());
            Long bankFreeBalance = bankInfo.getFreeBalance();
            if (bankFreeBalance != null) {
                freeBalance = bankFreeBalance;
            } else if (serverConfig.isDropFreeBalance()) {
                freeBalance = getStartFreeBalance(gameInfo, currencyCode);
            } else {
                freeBalance = serverConfig.getFreeBalance();
            }
        }
        return freeBalance;
    }

    public long getRoundBalance(long freeBalance) {
        long divisor = getDivisor(freeBalance);
        if (freeBalance > (divisor * 9)) {
            return divisor * 10;
        }
        return (long) Math.ceil((double) freeBalance / divisor) * divisor;
    }

    private long getDivisor(long freeBalance) {
        long number = freeBalance;
        long exponent = 0;
        while (number > 10) {
            number /= 10;
            exponent++;
        }
        number = 1;
        for (int j = 0; j < exponent; j++) {
            number *= 10;
        }
        return number;
    }

    private long getStartFreeBalance(IBaseGameInfo<?, ?> gameInfo, String currencyCode) {
        long startBalance = serverConfig.getFreeBalance();
        try {
            if (gameInfoTemplateCache.getBaseGameInfoTemplateById(gameInfo.getId()).isDynamicLevelsSupported()) {
                startBalance = (long) this.currencyConverter.convert(startBalance,
                        ICurrencyRateManager.DEFAULT_CURRENCY, currencyCode);
                if (startBalance < DEFAULT_FREE_BALANCE) {
                    startBalance = DEFAULT_FREE_BALANCE;
                }
            }
        } catch (CommonException e) {
            LOG.error("Error during convert currency rate:", e);
        }
        return startBalance;
    }
}
