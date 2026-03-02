package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.gameconfig.GameConfig;
import com.betsoft.casino.mp.model.gameconfig.IGameConfig;
import com.betsoft.casino.mp.model.gameconfig.ISpawnConfig;
import com.betsoft.casino.mp.service.IGameConfigService;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.abs.casino.common.exception.CommonException;

import java.util.List;

/**
 * User: flsh
 * Date: 21.09.17.
 */
public interface IGame<ENEMY extends IEnemy, SEAT extends ISeat, ENEMY_TYPE extends IEnemyType> {

    IShootResult doShoot(ENEMY enemy, SEAT seat, Money stake, boolean isBossRound,
                         boolean isNearLandMine, double damageMultiplier, ITransportObjectsFactoryService toService)
            throws CommonException;

    default IShootResult doShootWithExplode(ENEMY enemy, SEAT seat, int explodeDamage,
                                            ITransportObjectsFactoryService toService) throws CommonException {
        return null;
    }

    List<ENEMY_TYPE> getBaseEnemyTypes();

    default GameConfig getCurrentGameConfig(IGameConfigService gameConfigService) {
        return null;
    }

    default IGameConfig getGameConfig(long roomId) {
        return null;
    }

    default ISpawnConfig getSpawnConfig(long roomId) {
        return null;
    }
}
