package com.betsoft.casino.mp.bgmissionamazon.service;

import com.betsoft.casino.mp.bgmissionamazon.model.GameMap;
import com.betsoft.casino.mp.bgmissionamazon.model.GameRoom;
import com.betsoft.casino.mp.bgmissionamazon.model.GameRoomSnapshot;
import com.betsoft.casino.mp.bgmissionamazon.model.Seat;
import com.betsoft.casino.mp.bgmissionamazon.model.math.EnemyRange;
import com.betsoft.casino.mp.bgmissionamazon.model.math.config.GameConfig;
import com.betsoft.casino.mp.bgmissionamazon.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.bgmissionamazon.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.bgmissionamazon.model.math.config.SpawnConfigLoader;
import com.betsoft.casino.mp.common.AbstractRoomService;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.hazelcast.core.HazelcastInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;


public class BGMissionAmazonRoomService extends AbstractRoomService<GameRoom, GameRoomSnapshot, Seat, ISingleNodeRoomInfo> {
    private static final Logger LOG = LogManager.getLogger(BGMissionAmazonRoomService.class);

    public BGMissionAmazonRoomService(ApplicationContext context, HazelcastInstance hazelcast, String loggerDir,
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
        GameMap currentMap = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getStartMap(GameType.BG_MISSION_AMAZON));
        return new GameRoom(context, createRoomLogger(roomInfo.getId(), GameType.BG_MISSION_AMAZON, roomInfo.getMoneyType()),
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
        return new GameRoom(context, createRoomLogger(roomInfo.getId(), GameType.BG_MISSION_AMAZON, roomInfo.getMoneyType()),
                roomInfo, playerStatsService, weaponService, remoteExecutorService,
                snapshot, playerQuestsService, playerProfileService, gameConfigService,
                activeFrbSessionService, activeCashBonusSessionService, tournamentService, gameConfigProvider, spawnConfigProvider);
    }

    @Override
    public GameType getType() {
        return GameType.BG_MISSION_AMAZON;
    }

    @Override
    public synchronized void init() {
        GameConfig config = new GameConfigLoader().loadDefaultConfig();
        gameConfigProvider.registerDefaultConfig(GameType.BG_MISSION_AMAZON.getGameId(), config);
        SpawnConfig spawnConfig = new SpawnConfigLoader().loadDefaultConfig();
        spawnConfigProvider.registerDefaultConfig(GameType.BG_MISSION_AMAZON.getGameId(), spawnConfig);
        super.init();
    }
}
