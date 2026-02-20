package com.betsoft.casino.mp.bgsectorx.service;

import com.betsoft.casino.mp.common.AbstractRoomService;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.bgsectorx.model.GameMap;
import com.betsoft.casino.mp.bgsectorx.model.GameRoom;
import com.betsoft.casino.mp.bgsectorx.model.GameRoomSnapshot;
import com.betsoft.casino.mp.bgsectorx.model.Seat;
import com.betsoft.casino.mp.bgsectorx.model.math.EnemyRange;
import com.betsoft.casino.mp.bgsectorx.model.math.config.GameConfig;
import com.betsoft.casino.mp.bgsectorx.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.bgsectorx.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.bgsectorx.model.math.config.SpawnConfigLoader;
import com.betsoft.casino.mp.service.*;
import com.hazelcast.core.HazelcastInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;


public class BGSectorXRoomService extends AbstractRoomService<GameRoom, GameRoomSnapshot, Seat, ISingleNodeRoomInfo> {
    private static final Logger LOG = LogManager.getLogger(BGSectorXRoomService.class);

    public BGSectorXRoomService(ApplicationContext context, HazelcastInstance hazelcast, String loggerDir,
                              IGameRoomSnapshotPersister snapshotPersister,
                              IGameConfigProvider gameConfigProvider, ISpawnConfigProvider spawnConfigProvider) {
        super(context, hazelcast, loggerDir, snapshotPersister, gameConfigProvider, spawnConfigProvider);
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
        GameMap currentMap = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getStartMap(GameType.BG_SECTOR_X));
        return new GameRoom(context, createRoomLogger(roomInfo.getId(), GameType.BG_SECTOR_X, roomInfo.getMoneyType()),
                roomInfo, currentMap, playerStatsService,
                weaponService, remoteExecutorService, playerQuestsService, playerProfileService,
                gameConfigService, activeFrbSessionService, activeCashBonusSessionService, tournamentService,
                gameConfigProvider, spawnConfigProvider);
    }

    @Override
    protected GameRoom repair(ISingleNodeRoomInfo roomInfo, IPlayerStatsService playerStatsService, IWeaponService weaponService,
                              IPlayerQuestsService playerQuestsService,
                              IPlayerProfileService playerProfileService, GameRoomSnapshot snapshot,
                              IGameConfigService gameConfigService, IActiveFrbSessionService activeFrbSessionService,
                              IActiveCashBonusSessionService activeCashBonusSessionService,
                              ITournamentService tournamentService) {
        return new GameRoom(context, createRoomLogger(roomInfo.getId(), GameType.BG_SECTOR_X, roomInfo.getMoneyType()),
                roomInfo, playerStatsService, weaponService, remoteExecutorService,
                snapshot, playerQuestsService, playerProfileService, gameConfigService,
                activeFrbSessionService, activeCashBonusSessionService, tournamentService, gameConfigProvider, spawnConfigProvider);
    }

    @Override
    public GameType getType() {
        return GameType.BG_SECTOR_X;
    }

    @Override
    public synchronized void init() {
        GameConfig config = new GameConfigLoader().loadDefaultConfig();
        gameConfigProvider.registerDefaultConfig(GameType.BG_SECTOR_X.getGameId(), config);
        SpawnConfig spawnConfig = new SpawnConfigLoader().loadDefaultConfig();
        spawnConfigProvider.registerDefaultConfig(GameType.BG_SECTOR_X.getGameId(), spawnConfig);
        super.init();
    }
}
