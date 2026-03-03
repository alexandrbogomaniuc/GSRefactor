package com.betsoft.casino.mp.maxcrashgame.model;

import com.betsoft.casino.mp.common.AbstractEnemyGame;
import com.betsoft.casino.mp.common.ShootResult;
import com.betsoft.casino.mp.common.Weapon;
import com.betsoft.casino.mp.maxcrashgame.model.math.EnemyType;
import com.betsoft.casino.mp.maxcrashgame.model.math.config.GameConfig;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.gameconfig.IGameConfig;
import com.betsoft.casino.mp.service.IGameConfigProvider;
import com.betsoft.casino.mp.service.IGameConfigService;
import com.betsoft.casino.mp.service.ISpawnConfigProvider;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.betsoft.casino.teststand.TestStandFeature;
import com.betsoft.casino.teststand.TestStandLocal;
import com.abs.casino.common.exception.CommonException;
import org.apache.logging.log4j.Logger;
import java.util.*;

public class EnemyGame extends AbstractEnemyGame<Enemy, Seat, EnemyType> {
    private final IGameConfigProvider gameConfigProvider;
    private final ISpawnConfigProvider spawnConfigProvider;

    public EnemyGame(Logger logger, IGameConfigService<? extends IGameConfigService<?>> gameConfigService,
                     IGameConfigProvider gameConfigProvider, ISpawnConfigProvider spawnConfigProvider) {
        super(logger, GameType.MAXCRASHGAME, gameConfigService);
        this.gameConfigProvider = gameConfigProvider;
        this.spawnConfigProvider = spawnConfigProvider;
    }

    @Override
    public List<EnemyType> getBaseEnemyTypes() {
        return Collections.emptyList();
    }

    @Override
    protected ShootResult shootBaseEnemy(Seat seat, Weapon weapon, Money stake, Enemy enemy, boolean isBot,
                                         boolean isBossRound, boolean isNearLandMine, double damageMultiplier,
                                         ITransportObjectsFactoryService toService) {
        throw new UnsupportedOperationException("Shot without room mode is not allowed");
    }

    private MaxCrashStoneTestStand getTestStand(long accountId, String sessionId) {
        if (sessionId != null) {
            TestStandFeature featureBySid = TestStandLocal.getInstance().getFeatureBySid(sessionId);
            logMessage(accountId, " testStand feature: {}", featureBySid);
            if (featureBySid != null) {
                return new MaxCrashStoneTestStand(featureBySid);
            }
        }
        return new MaxCrashStoneTestStand();
    }

    @Override
    protected boolean isRealBet(Weapon weapon) {
        return true;
    }

    @Override
    public IShootResult doShoot(Enemy enemy, Seat seat, Money stake, boolean isBossRound, boolean isNearLandMine, double damageMultiplier,
                                ITransportObjectsFactoryService toService) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IShootResult doShootWithExplode(Enemy enemy, Seat seat, int explodeDamage,
                                           ITransportObjectsFactoryService toService) {
        throw new UnsupportedOperationException();
    }


    public GameConfig getConfig(Seat seat) {
        return (GameConfig) gameConfigProvider
                .getConfig(GameType.MAXCRASHGAME.getGameId(), seat.getPlayerInfo().getRoomId());
    }

    public GameConfig getConfig(long roomId) {
        return (GameConfig) gameConfigProvider.getConfig(GameType.MAXCRASHGAME.getGameId(), roomId);
    }

    @Override
    public IGameConfig getGameConfig(long roomId) {
        return getConfig(roomId);
    }

}
