package com.betsoft.casino.mp.dragonstone.service;

import com.betsoft.casino.mp.common.AbstractRoomService;
import com.betsoft.casino.mp.dragonstone.model.GameMap;
import com.betsoft.casino.mp.dragonstone.model.GameRoom;
import com.betsoft.casino.mp.dragonstone.model.GameRoomSnapshot;
import com.betsoft.casino.mp.dragonstone.model.Seat;
import com.betsoft.casino.mp.dragonstone.model.math.EnemyRange;
import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfig;
import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.dragonstone.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.dragonstone.model.math.config.SpawnConfigLoader;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.hazelcast.core.HazelcastInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;


public class DragonStoneRoomService extends AbstractRoomService<GameRoom, GameRoomSnapshot, Seat, ISingleNodeRoomInfo> {
    private static final Logger LOG = LogManager.getLogger(DragonStoneRoomService.class);

    public DragonStoneRoomService(ApplicationContext context, HazelcastInstance hazelcast, String loggerDir,
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
        GameMap currentMap = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getStartMap(GameType.DRAGONSTONE));
        return new GameRoom(context, createRoomLogger(roomInfo.getId(), GameType.DRAGONSTONE, roomInfo.getMoneyType()),
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
        return new GameRoom(context, createRoomLogger(roomInfo.getId(), GameType.DRAGONSTONE, roomInfo.getMoneyType()),
                roomInfo, playerStatsService, weaponService, remoteExecutorService,
                snapshot, playerQuestsService, playerProfileService, gameConfigService, activeFrbSessionService,
                activeCashBonusSessionService, tournamentService, gameConfigProvider, spawnConfigProvider);
    }

    @Override
    public GameType getType() {
        return GameType.DRAGONSTONE;
    }

    @Override
    public synchronized void init() {
        GameConfig config = new GameConfigLoader().loadDefaultConfig();
        gameConfigProvider.registerDefaultConfig(GameType.DRAGONSTONE.getGameId(), config);
        SpawnConfig spawnConfig = new SpawnConfigLoader().loadDefaultConfig();
        spawnConfigProvider.registerDefaultConfig(GameType.DRAGONSTONE.getGameId(), spawnConfig);
        super.init();
    }
}
