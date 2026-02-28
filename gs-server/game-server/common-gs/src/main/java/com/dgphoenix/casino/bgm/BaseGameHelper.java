package com.abs.casino.bgm;

import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.abs.casino.common.cache.data.bank.ILimit;
import com.abs.casino.common.cache.data.game.ImmutableBaseGameInfoWrapper;
import com.dgphoenix.casino.common.cache.data.bank.Limit;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.*;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: plastical
 * Date: 29.03.2010
 */
public class BaseGameHelper {
    private static final Logger LOG = LogManager.getLogger(BaseGameHelper.class);

    public static IBaseGameInfo createGame(long bankId, long gameId, Currency currency, String gameName,
                                           GameType type, GameGroup group, GameVariableType variableType,
                                           String rmClassName, String spClassName,
                                           Map<String, String> properties,
                                           ILimit limit, List<Coin> gameCoins,
                                           boolean createJackpot, Double pcrp, Double bcrp) throws CommonException {
        ArrayList<String> langs = new ArrayList<>();
        langs.add("en");
        return createGame(bankId, gameId, currency, gameName, type, group, variableType, rmClassName,
                spClassName, properties, limit, gameCoins, createJackpot, pcrp, bcrp, langs);
    }

    public static IBaseGameInfo createGame(long bankId, long gameId, Currency currency, String gameName,
                                           GameType type, GameGroup group, GameVariableType variableType,
                                           String rmClassName, String spClassName, Map<String, String> properties,
                                           ILimit limit, List<Coin> gameCoins, boolean createJackpot, Double pcrp,
                                           Double bcrp, List<String> langs) throws CommonException {
        final IBaseGameInfo baseGameInfo = BaseGameCache.getInstance().getGameInfo(bankId, gameId, currency);
        if (baseGameInfo == null || baseGameInfo instanceof ImmutableBaseGameInfoWrapper) {
            LOG.info("BaseGameHelper::createGame initializing new game id:" + gameId + " bankId:" + bankId);
            if (langs == null || langs.isEmpty()) {
                langs = new ArrayList<>();
                langs.add("en");
            }

            BaseGameInfo gameInfo = new BaseGameInfo(gameId, bankId, gameName, type, group, variableType,
                    rmClassName, spClassName, (Limit) limit, gameCoins, properties, currency, langs);
            BaseGameCache.getInstance().put(gameInfo);
            RemoteCallHelper.getInstance().saveAndSendNotification(gameInfo);

            return gameInfo;
        }
        return baseGameInfo;
    }
}
