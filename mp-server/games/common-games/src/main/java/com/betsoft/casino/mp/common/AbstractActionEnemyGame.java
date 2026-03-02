package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.service.IGameConfigService;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.abs.casino.common.exception.CommonException;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * User: flsh
 * Date: 29.04.2022.
 */

/**
 * Class AbstractActionEnemyGame for action games
 * @param <ENEMY> enemy
 * @param <SEAT> seat
 * @param <ENEMY_TYPE> enemyType
 */
public abstract class AbstractActionEnemyGame<ENEMY extends IEnemy, SEAT extends IActionGameSeat, ENEMY_TYPE extends IEnemyType>
        extends AbstractEnemyGame<ENEMY, SEAT, ENEMY_TYPE> {
    protected AbstractActionEnemyGame(Logger logger, GameType gameType, IGameConfigService gameConfigService) {
        super(logger, gameType, gameConfigService);
    }

    /**
     * Makes shot from play game state.
     * @param enemy target enemy
     * @param seat seat of player
     * @param stake stake
     * @param isBossRound is boss round or not
     * @param isNearLandMine true if there are mines nearby
     * @param damageMultiplier additional damage multiplier
     * @param toService transport service for generation of transport objects.
     * @return {@code ShootResult} result of shot
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public ShootResult doShoot(ENEMY enemy, SEAT seat, Money stake, boolean isBossRound,
                               boolean isNearLandMine, double damageMultiplier, ITransportObjectsFactoryService toService)
            throws CommonException {

        long accountId = seat.getAccountId();
        String sessionId = seat.getPlayerInfo().getSessionId();

        boolean isBot = seat.getSocketClient().isBot();

        if (debug) {
            logMessage(seat.getAccountId(), "doShoot, sessionId: " + sessionId
                    + " damageMultiplier: " + damageMultiplier
                    + " seat.getCurrentWeaponId(): " + seat.getCurrentWeaponId()
            );
        }

        Weapon weapon = (Weapon) seat.getCurrentWeapon();
        logMessage(accountId, "doShoot,  weapon: " + weapon);

        ShootResult res = shootBaseEnemy(seat, weapon, stake, enemy, isBot, isBossRound, isNearLandMine,
                damageMultiplier, toService);

        if (res.isDestroyed()) {
            seat.incCountEnemiesKilled();
            IPlayerStats roundStats = seat.getPlayerInfo().getRoundStats();
            Map<Integer, Long> roundStatsKills = roundStats.getKills();
            roundStatsKills.put(0, roundStatsKills.isEmpty() ? 1 : roundStatsKills.get(0) + 1);
        }

        if (debug) {
            logMessage(accountId, "doShoot shootResult: " + res);
        }
        return res;
    }
}
