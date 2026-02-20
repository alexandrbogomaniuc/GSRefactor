package com.betsoft.casino.mp.pirates.service;

import com.betsoft.casino.mp.common.AbstractRoomService;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.pirates.model.GameMap;
import com.betsoft.casino.mp.pirates.model.GameRoom;
import com.betsoft.casino.mp.pirates.model.GameRoomSnapshot;
import com.betsoft.casino.mp.pirates.model.Seat;
import com.betsoft.casino.mp.pirates.model.math.EnemyRange;
import com.betsoft.casino.mp.service.*;
import com.hazelcast.core.HazelcastInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

/**
 * User: flsh
 * Date: 03.11.17.
 */
public class PiratesRoomService extends AbstractRoomService<GameRoom, GameRoomSnapshot, Seat, ISingleNodeRoomInfo> {
    private static final Logger LOG = LogManager.getLogger(PiratesRoomService.class);

    public PiratesRoomService(ApplicationContext context, HazelcastInstance hazelcast, String loggerDir,
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
        GameMap currentMap = new GameMap(EnemyRange.BaseEnemies, gameMapStore.getStartMap(GameType.PIRATES));
        return new GameRoom(context, createRoomLogger(roomInfo.getId(), GameType.PIRATES, roomInfo.getMoneyType()),
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
        return new GameRoom(context, createRoomLogger(roomInfo.getId(), GameType.PIRATES, roomInfo.getMoneyType()),
                roomInfo, playerStatsService, weaponService,
                remoteExecutorService, snapshot, playerQuestsService, playerProfileService,
                gameConfigService, activeFrbSessionService, activeCashBonusSessionService, tournamentService);
    }

    @Override
    public GameType getType() {
        return GameType.PIRATES;
    }
}
