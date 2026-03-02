package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.gameconfig.GameConfig;
import com.betsoft.casino.mp.service.IGameConfigService;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.RNG;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * User: flsh
 * Date: 15.02.19.
 */

/**
 * Abstract class for game. Calculate game logic of shots. Work with objects seat, enemy, enemyType.
 * @param <ENEMY> target enemy
 * @param <SEAT>  the player who fired the shot
 * @param <ENEMY_TYPE> enemyType
 */
public abstract class AbstractEnemyGame<ENEMY extends IEnemy, SEAT extends ISeat, ENEMY_TYPE extends IEnemyType>
        implements IGame<ENEMY, SEAT, ENEMY_TYPE> {
    protected boolean debug = true;
    protected GameConfig currentGameConfig;
    protected final Logger logger;
    protected IGameConfigService gameConfigService;

    protected AbstractEnemyGame(Logger logger, GameType gameType, IGameConfigService gameConfigService) {
        this.logger = logger;
        this.gameConfigService = gameConfigService;
        currentGameConfig = getCurrentGameConfig(this.gameConfigService);
        logger.debug("currentGameConfig: {}", currentGameConfig);
    }

    /**
     * abstract method for shot
     * @param seat player
     * @param weapon special weapon
     * @param stake stake
     * @param enemy enemy for shot
     * @param isBot isBot
     * @param isBossRound isBossRound
     * @param isNearLandMine isNearLandMine
     * @param damageMultiplier additional damageMultiplier for calculation
     * @param toService transport service for creation additional messages
     * @return {@code ShootResult } result of shot to enemy.
     * @throws CommonException
     */
    protected abstract ShootResult shootBaseEnemy(SEAT seat, Weapon weapon, Money stake, ENEMY enemy,
                                                  boolean isBot, boolean isBossRound,
                                                  boolean isNearLandMine, double damageMultiplier,
                                                  ITransportObjectsFactoryService toService) throws CommonException;

    protected void logMessage(long aid, String message) {
        if (debug) {
            logger.debug("aid: {}: {}", aid, message);
        }
    }

    protected void logMessage(long aid, String message, Object... params) {
        if (debug) {
            logger.debug("aid: " + aid + ": " + message, params);
        }
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    protected abstract boolean isRealBet(Weapon weapon);

    /**
     * Return game config service for game
     * @return {@code IGameConfigService} game config service
     */
    public IGameConfigService getGameConfigService() {
        return gameConfigService;
    }

}
