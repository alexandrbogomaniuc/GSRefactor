package com.betsoft.casino.mp.revengeofra.service;

import com.betsoft.casino.mp.common.AbstractRoomService;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.revengeofra.model.GameMap;
import com.betsoft.casino.mp.revengeofra.model.GameRoom;
import com.betsoft.casino.mp.revengeofra.model.GameRoomSnapshot;
import com.betsoft.casino.mp.revengeofra.model.Seat;
import com.betsoft.casino.mp.revengeofra.model.math.EnemyRange;
import com.betsoft.casino.mp.service.*;
import com.hazelcast.core.HazelcastInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;


public class RevengeOfRaRoomService extends AbstractRoomService<GameRoom, GameRoomSnapshot, Seat, ISingleNodeRoomInfo> {
    private static final Logger LOG = LogManager.getLogger(RevengeOfRaRoomService.class);

    public RevengeOfRaRoomService(ApplicationContext context, HazelcastInstance hazelcast, String loggerDir,
                                  IGameRoomSnapshotPersister snapshotPersister) {
        super(context, hazelcast, loggerDir, snapshotPersister, null, null);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected GameRoom create(ISingleNodeRoomInfo roomInfo, IPlayerStatsService playerStatsService, IWeaponService weaponService,
                              IPlayerQuestsService playerQuestsService,
                              IPlayerProfileService playerProfileService, IGameConfigService gameConfigService,
                              IActiveFrbSessionService activeFrbSessionService,
                              IActiveCashBonusSessionService activeCashBonusSessionService,
                              ITournamentService tournamentService) {
        GameMap currentMap = new GameMap(EnemyRange.BaseEnemies, gameMapStore.getStartMap(GameType.REVENGE_OF_RA));
        return new GameRoom(context, createRoomLogger(roomInfo.getId(), GameType.REVENGE_OF_RA, roomInfo.getMoneyType()),
                roomInfo, currentMap, playerStatsService,
                weaponService, remoteExecutorService, playerQuestsService, playerProfileService,
                gameConfigService, activeFrbSessionService, activeCashBonusSessionService, tournamentService);
    }

    @Override
    protected GameRoom repair(ISingleNodeRoomInfo roomInfo, IPlayerStatsService playerStatsService, IWeaponService weaponService,
                              IPlayerQuestsService playerQuestsService,
                              IPlayerProfileService playerProfileService, GameRoomSnapshot snapshot,
                              IGameConfigService gameConfigService, IActiveFrbSessionService activeFrbSessionService,
                              IActiveCashBonusSessionService activeCashBonusSessionService,
                              ITournamentService tournamentService) {
        return new GameRoom(context, createRoomLogger(roomInfo.getId(), GameType.REVENGE_OF_RA, roomInfo.getMoneyType()),
                roomInfo, playerStatsService, weaponService,
                remoteExecutorService, snapshot, playerQuestsService, playerProfileService,
                gameConfigService, activeFrbSessionService, activeCashBonusSessionService, tournamentService);
    }

    @Override
    public GameType getType() {
        return GameType.REVENGE_OF_RA;
    }
}
