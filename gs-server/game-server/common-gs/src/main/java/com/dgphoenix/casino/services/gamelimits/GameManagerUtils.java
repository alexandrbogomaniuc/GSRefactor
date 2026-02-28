package com.abs.casino.services.gamelimits;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.account.PlayerDeviceType;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.*;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.util.StreamUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.services.ServiceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameManagerUtils {

    private static final Logger LOG = LogManager.getLogger(GameManagerUtils.class);
    private final BankInfoCache bankInfoCache;
    private final BaseGameCache baseGameCache;
    private final MQServiceHandler mqServiceHandler;
    private static final List<Long> CRASH_GAMES = Arrays.asList(863L, 875L, 30429L);

    public GameManagerUtils(BankInfoCache bankInfoCache, BaseGameCache baseGameCache, MQServiceHandler mqServiceHandler) {
        this.bankInfoCache = bankInfoCache;
        this.baseGameCache = baseGameCache;
        this.mqServiceHandler = mqServiceHandler;
    }

    public BankInfo getBankWithCheck(long bankId) throws CommonException {
        BankInfo bankInfo = bankInfoCache.getBankInfo(bankId);
        if (bankInfo == null) {
            throw new CommonException("bank doesn't exist");
        }
        return bankInfo;
    }

    public Currency getCurrency(long bankId, String currencyCode) throws CommonException {
        return ServiceUtils.getCurrency(bankId, currencyCode);
    }

    public IBaseGameInfo createGameInfo(long bankId, long gameId, Currency currency) throws CommonException {
        IBaseGameInfo defaultGameInfoShared = baseGameCache.getGameInfo(bankId, gameId, (Currency) null);
        if (defaultGameInfoShared == null) {
            throw new CommonException("game is not supported in casino");
        }

        IBaseGameInfo newGameInfo = defaultGameInfoShared.copy();
        newGameInfo.setCurrency(currency);
        baseGameCache.put((BaseGameInfo) newGameInfo);
        return newGameInfo;
    }

    public List<Long> getToGoGameIds(BaseGameInfoTemplate template) {
        String androidGameId = template.getDefaultGameInfo().getProperty(PlayerDeviceType.ANDROID.name());
        String iOSGameId = template.getDefaultGameInfo().getProperty(PlayerDeviceType.IOSMOBILE.name());
        String winPhoneGameId = template.getDefaultGameInfo().getProperty(PlayerDeviceType.WINDOWSPHONE.name());
        return Stream.of(androidGameId, iOSGameId, winPhoneGameId)
                .filter(Objects::nonNull)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    public boolean isWinLimitCalculated(IBaseGameInfo gameInfo) {
        //need check mqServiceHandler for prevent NPE, GameConfigurationManager created directly from scripts
        return mqServiceHandler != null && CRASH_GAMES.contains(gameInfo.getId());
    }

    public double getMaxWin(IBaseGameInfo gameInfo, BaseGameInfoTemplate template, BankInfo bankInfo, Currency currency, boolean winLimitCalculated) {
        String maxWinProperty = template.getDefaultGameInfo().getProperty(BaseGameConstants.KEY_MAX_WIN);
        double maxWin = 0;
        if (winLimitCalculated) {
            try {
                maxWin = mqServiceHandler.getCrashMaxProfit(gameInfo, bankInfo, currency);
            } catch (CommonException e) {
                LOG.error("getMaxWin: cannot load CrashMaxProfit for game={}", gameInfo, e);
            }
        } else if (!StringUtils.isTrimmedEmpty(maxWinProperty)){
            maxWin = Double.parseDouble(maxWinProperty);
        }
        return maxWin;
    }

    public List<Integer> getPossibleLines(BaseGameInfoTemplate template) {
        if (GameGroup.VIDEOPOKER.equals(template.getDefaultGameInfo().getGroup())) {
            return Collections.singletonList(1);
        }
        String possibleLines = template.getDefaultGameInfo().getProperty(BaseGameConstants.KEY_POSSIBLE_LINES);
        return StringUtils.isTrimmedEmpty(possibleLines) ? null : StreamUtils.asStream(possibleLines, "|")
                .map(Integer::parseInt)
                .sorted()
                .collect(Collectors.toList());

    }

    public List<Integer> getPossibleBetPerLines(BaseGameInfoTemplate template) {
        if (GameGroup.VIDEOPOKER.equals(template.getDefaultGameInfo().getGroup())) {
            String maxBetInCredits = template.getDefaultGameInfo().getProperty(BaseGameConstants.KEY_MAX_BET_IN_CREDITS);
            return StringUtils.isTrimmedEmpty(maxBetInCredits) ? Collections.singletonList(1) : Arrays.asList(1, Integer.parseInt(maxBetInCredits));
        }
        String possibleBetPerLines = template.getDefaultGameInfo().getProperty(BaseGameConstants.KEY_POSSIBLE_BETPERLINES);
        return StringUtils.isTrimmedEmpty(possibleBetPerLines) ? null : StreamUtils.asStream(possibleBetPerLines, "|")
                .map(Integer::parseInt)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Double> getChips(IBaseGameInfo gameInfo) {
        String chips = gameInfo.getChipValues();
        return StringUtils.isTrimmedEmpty(chips) ? null : StreamUtils.asStream(chips, "|")
                .map(Double::parseDouble)
                .sorted()
                .collect(Collectors.toList());
    }

    public long getDefaultCredits(IBaseGameInfo gameInfo) {
        String sDefaultBpl = gameInfo.getProperty(BaseGameConstants.KEY_DEFAULTBETPERLINE);
        long defaultBpl = StringUtils.isTrimmedEmpty(sDefaultBpl) ? 1 : Long.parseLong(sDefaultBpl);
        String sDefaultLines = gameInfo.getProperty(BaseGameConstants.KEY_DEFAULTNUMLINES);
        long defaultLines = StringUtils.isTrimmedEmpty(sDefaultLines) ? 1 : Long.parseLong(sDefaultLines);
        return defaultBpl * defaultLines;
    }

}
