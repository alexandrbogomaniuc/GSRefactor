package com.betsoft.casino.mp.maxcrashgame.service;

import com.betsoft.casino.mp.common.AbstractRoomService;
import com.betsoft.casino.mp.maxcrashgame.model.GameMap;
import com.betsoft.casino.mp.maxcrashgame.model.GameRoomSnapshot;
import com.betsoft.casino.mp.maxcrashgame.model.Seat;
import com.betsoft.casino.mp.maxcrashgame.model.TripleMaxBlastGameRoom;
import com.betsoft.casino.mp.maxcrashgame.model.math.EnemyRange;
import com.betsoft.casino.mp.maxcrashgame.model.math.config.GameConfig;
import com.betsoft.casino.mp.maxcrashgame.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.room.IMultiNodeRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.hazelcast.core.HazelcastInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

public class TripleMaxBlastRoomService extends AbstractRoomService<TripleMaxBlastGameRoom, GameRoomSnapshot, Seat, IMultiNodeRoomInfo> {
    private static final Logger LOG = LogManager.getLogger(TripleMaxBlastRoomService.class);

    public TripleMaxBlastRoomService(ApplicationContext context, HazelcastInstance hazelcast, String loggerDir,
                                     IGameRoomSnapshotPersister snapshotPersister,
                                     IGameConfigProvider gameConfigProvider, ISpawnConfigProvider spawnConfigProvider) {
        super(context, hazelcast, loggerDir, snapshotPersister, gameConfigProvider, spawnConfigProvider);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected TripleMaxBlastGameRoom create(IMultiNodeRoomInfo roomInfo, IPlayerStatsService playerStatsService, IWeaponService weaponService,
                              IPlayerQuestsService playerQuestsService,
                              IPlayerProfileService playerProfileService, IGameConfigService gameConfigService,
                              IActiveFrbSessionService activeFrbSessionService,
                              IActiveCashBonusSessionService activeCashBonusSessionService,
                              ITournamentService tournamentService) {
        GameMap currentMap = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getStartMap(GameType.TRIPLE_MAX_BLAST));
        return new TripleMaxBlastGameRoom(context, createRoomLogger(roomInfo.getId(), GameType.TRIPLE_MAX_BLAST, roomInfo.getMoneyType()),
                roomInfo, currentMap, playerStatsService,
                weaponService, remoteExecutorService, playerQuestsService, playerProfileService,
                gameConfigService, activeFrbSessionService, activeCashBonusSessionService, tournamentService,
                gameConfigProvider, spawnConfigProvider);
    }

    @Override
    protected TripleMaxBlastGameRoom repair(IMultiNodeRoomInfo roomInfo, IPlayerStatsService playerStatsService, IWeaponService weaponService,
                              IPlayerQuestsService playerQuestsService,
                              IPlayerProfileService playerProfileService, GameRoomSnapshot snapshot,
                              IGameConfigService gameConfigService, IActiveFrbSessionService activeFrbSessionService,
                              IActiveCashBonusSessionService activeCashBonusSessionService,
                              ITournamentService tournamentService) {
        return new TripleMaxBlastGameRoom(context, createRoomLogger(roomInfo.getId(), GameType.TRIPLE_MAX_BLAST, roomInfo.getMoneyType()),
                roomInfo, playerStatsService, weaponService, remoteExecutorService,
                snapshot, playerQuestsService, playerProfileService, gameConfigService, activeFrbSessionService,
                activeCashBonusSessionService, tournamentService, gameConfigProvider, spawnConfigProvider);
    }

    @Override
    public GameType getType() {
        return GameType.TRIPLE_MAX_BLAST;
    }

    @Override
    public synchronized void init() {
        GameConfig config = new GameConfigLoader().loadDefaultConfig();
        gameConfigProvider.registerDefaultConfig(GameType.TRIPLE_MAX_BLAST.getGameId(), config);
        super.init();
    }
}
