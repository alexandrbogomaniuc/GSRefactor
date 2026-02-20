package com.betsoft.casino.mp.piratespov.model;

import com.betsoft.casino.mp.common.AbstractActionEnemyGame;
import com.betsoft.casino.mp.common.AbstractEnemyGame;
import com.betsoft.casino.mp.common.ShootResult;
import com.betsoft.casino.mp.common.Weapon;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.model.gameconfig.GameConfig;
import com.betsoft.casino.mp.piratescommon.model.math.EnemyType;
import com.betsoft.casino.mp.piratescommon.model.math.ShotCalculator;
import com.betsoft.casino.mp.service.IGameConfigService;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.dgphoenix.casino.common.exception.CommonException;
import org.apache.logging.log4j.Logger;
import java.util.List;

public class EnemyGame extends AbstractActionEnemyGame<Enemy, Seat, EnemyType> {

    public EnemyGame(Logger logger, IGameConfigService gameConfigService) {
        super(logger, GameType.PIRATES_POV, gameConfigService);
    }


    @Override
    public List<EnemyType> getBaseEnemyTypes() {
        return EnemyRange.BaseEnemies.getEnemies();
    }

    @Override
    protected ShootResult shootBaseEnemy(Seat seat, Weapon weapon, Money stake, Enemy enemy,
                                         boolean isBot, boolean isBossRound, boolean isNearLandMine,
                                         double totalDamageMultiplier, ITransportObjectsFactoryService toService)
            throws CommonException {
        GameConfig currentGameConfig = getCurrentGameConfig(getGameConfigService());
        return ShotCalculator.shootBaseEnemy(seat, weapon, stake, enemy, isBot, isBossRound, isNearLandMine,
                totalDamageMultiplier, toService, currentGameConfig, logger);
    }

    @Override
    protected boolean isRealBet(Weapon weapon) {
        return true;
    }

    public GameConfig getCurrentGameConfig(IGameConfigService gameConfigService) {
        return null;
    }

}
