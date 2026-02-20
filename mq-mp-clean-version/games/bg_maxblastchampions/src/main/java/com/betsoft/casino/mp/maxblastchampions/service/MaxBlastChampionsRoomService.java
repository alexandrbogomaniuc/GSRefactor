package com.betsoft.casino.mp.maxblastchampions.service;

import com.betsoft.casino.mp.common.AbstractRoomService;
import com.betsoft.casino.mp.maxblastchampions.model.GameMap;
import com.betsoft.casino.mp.maxblastchampions.model.GameRoom;
import com.betsoft.casino.mp.maxblastchampions.model.GameRoomSnapshot;
import com.betsoft.casino.mp.maxblastchampions.model.Seat;
import com.betsoft.casino.mp.maxblastchampions.model.math.EnemyRange;
import com.betsoft.casino.mp.maxblastchampions.model.math.config.GameConfig;
import com.betsoft.casino.mp.maxblastchampions.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.room.IMultiNodeRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.hazelcast.core.HazelcastInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

public class MaxBlastChampionsRoomService extends AbstractRoomService<GameRoom, GameRoomSnapshot, Seat, IMultiNodeRoomInfo> {
    private static final Logger LOG = LogManager.getLogger(MaxBlastChampionsRoomService.class);

    public MaxBlastChampionsRoomService(ApplicationContext context, HazelcastInstance hazelcast, String loggerDir,
                               IGameRoomSnapshotPersister snapshotPersister,
                               IGameConfigProvider gameConfigProvider, ISpawnConfigProvider spawnConfigProvider) {
        super(context, hazelcast, loggerDir, snapshotPersister, gameConfigProvider, spawnConfigProvider);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected GameRoom create(IMultiNodeRoomInfo roomInfo, IPlayerStatsService playerStatsService, IWeaponService weaponService,
                              IPlayerQuestsService playerQuestsService,
                              IPlayerProfileService playerProfileService, IGameConfigService gameConfigService,
                              IActiveFrbSessionService activeFrbSessionService,
                              IActiveCashBonusSessionService activeCashBonusSessionService,
                              ITournamentService tournamentService) {

        GameMap currentMap = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getStartMap(GameType.BG_MAXCRASHGAME));
        Logger logger = this.createRoomLogger(roomInfo.getId(), GameType.BG_MAXCRASHGAME, roomInfo.getMoneyType());

        GameRoom gameRoom = new GameRoom(context, logger,
                roomInfo, currentMap, playerStatsService,
                weaponService, remoteExecutorService, playerQuestsService, playerProfileService,
                gameConfigService, activeFrbSessionService, activeCashBonusSessionService, tournamentService,
                gameConfigProvider, spawnConfigProvider);

        return gameRoom;
    }

    @Override
    protected GameRoom repair(IMultiNodeRoomInfo roomInfo, IPlayerStatsService playerStatsService, IWeaponService weaponService,
                              IPlayerQuestsService playerQuestsService,
                              IPlayerProfileService playerProfileService, GameRoomSnapshot snapshot,
                              IGameConfigService gameConfigService, IActiveFrbSessionService activeFrbSessionService,
                              IActiveCashBonusSessionService activeCashBonusSessionService,
                              ITournamentService tournamentService) {

        Logger logger = createRoomLogger(roomInfo.getId(), GameType.BG_MAXCRASHGAME, roomInfo.getMoneyType());
        GameRoom gameRoom = new GameRoom(context, logger,
                roomInfo, playerStatsService, weaponService, remoteExecutorService,
                snapshot, playerQuestsService, playerProfileService, gameConfigService, activeFrbSessionService,
                activeCashBonusSessionService, tournamentService, gameConfigProvider, spawnConfigProvider);

        return gameRoom;
    }

    @Override
    public GameType getType() {
        return GameType.BG_MAXCRASHGAME;
    }

    @Override
    public synchronized void init() {
        GameConfigLoader gameConfigLoader = new GameConfigLoader();
        GameConfig config = gameConfigLoader.loadDefaultConfig();
        gameConfigProvider.registerDefaultConfig(GameType.BG_MAXCRASHGAME.getGameId(), config);
        super.init();
    }
}
