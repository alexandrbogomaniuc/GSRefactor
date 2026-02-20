package com.betsoft.casino.mp.maxblastchampions.model;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.room.IMultiNodeRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.hazelcast.core.IExecutorService;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

@SuppressWarnings({"rawtypes"})
public class MaxBlastChampionsGameRoom extends BattleAbstractCrashGameRoom {
    public MaxBlastChampionsGameRoom(ApplicationContext context, Logger logger, IMultiNodeRoomInfo roomInfo, GameMap map,
                                  IPlayerStatsService playerStatsService, IWeaponService weaponService, IExecutorService remoteExecutorService,
                                  IPlayerQuestsService playerQuestsService, IPlayerProfileService playerProfileService,
                                  IGameConfigService gameConfigService, IActiveFrbSessionService activeFrbSessionService,
                                  IActiveCashBonusSessionService activeCashBonusSessionService, ITournamentService tournamentService,
                                  IGameConfigProvider gameConfigProvider, ISpawnConfigProvider spawnConfigProvider) {
        super(context, logger, roomInfo, map, playerStatsService, weaponService, remoteExecutorService, playerQuestsService,
                playerProfileService, gameConfigService, activeFrbSessionService, activeCashBonusSessionService, tournamentService,
                gameConfigProvider, spawnConfigProvider);
    }

    public MaxBlastChampionsGameRoom(ApplicationContext context, Logger logger, IMultiNodeRoomInfo roomInfo, IPlayerStatsService playerStatsService,
                                  IWeaponService weaponService, IExecutorService remoteExecutorService, GameRoomSnapshot snapshot,
                                  IPlayerQuestsService playerQuestsService, IPlayerProfileService playerProfileService,
                                  IGameConfigService gameConfigService, IActiveFrbSessionService activeFrbSessionService,
                                  IActiveCashBonusSessionService activeCashBonusSessionService, ITournamentService tournamentService,
                                  IGameConfigProvider gameConfigProvider, ISpawnConfigProvider spawnConfigProvider) {
        super(context, logger, roomInfo, playerStatsService, weaponService, remoteExecutorService, snapshot, playerQuestsService,
                playerProfileService, gameConfigService, activeFrbSessionService, activeCashBonusSessionService, tournamentService,
                gameConfigProvider, spawnConfigProvider);
    }

    @Override
    public GameType getGameType() {
        return GameType.BG_MAXCRASHGAME;
    }
}
