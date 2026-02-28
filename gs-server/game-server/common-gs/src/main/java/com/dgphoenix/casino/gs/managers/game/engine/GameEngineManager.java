/**
 * Created
 * Date: 28.11.2008
 * Time: 19:31:48
 */
package com.abs.casino.gs.managers.game.engine;

import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.util.ReflectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameEngineManager {
    private static final Logger LOG = LogManager.getLogger(GameEngineManager.class);
    private static GameEngineManager instance = new GameEngineManager();

    private final Map<Long, IGameEngine> engines = new ConcurrentHashMap<Long, IGameEngine>();

    public static GameEngineManager getInstance() {
        return instance;
    }

    private GameEngineManager() {
    }

    public IGameEngine getGameEngine(long bankId, long gameId) throws CommonException {
        IGameEngine gameEngine = engines.get(gameId);
        if (gameEngine == null) {
            synchronized (this) {
                gameEngine = engines.get(gameId);
                if (gameEngine == null) {
                    LOG.debug("GEM::getGameEngine engine for gameId:" + gameId + " doesn't exist, creating new one...");
                    try {
                        IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, gameId, null);
                        if (gameInfo == null) {
                            LOG.error("BaseGameInfo not found, gameId= " + gameId + ", bankId=" + bankId +
                                    ".Try use template BaseGameInfo");
                            gameInfo = BaseGameInfoTemplateCache.getInstance().getDefaultGameInfo(gameId);
                        }
                        String className = gameInfo.getGsClassName();
                        gameEngine = instantiate(className, gameId);
                        gameEngine.init();
                        engines.put(gameId, gameEngine);
                    } catch (Exception e) {
                        throw new CommonException("Get game info exception, gameId= " + gameId +
                                ", bankId=" + bankId, e);
                    }
                }
            }
        }
        return gameEngine;
    }

    private IGameEngine instantiate(String className, long gameId) throws CommonException {
        IGameEngine ge;
        try {
            ge = (IGameEngine) ReflectionUtils.forNameWithCompatibilityAliases(className)
                    .getConstructor(long.class).newInstance(gameId);
        } catch (Throwable e) {
            throw new CommonException("Instantiate GE exception", e);
        }
        return ge;
    }
}
