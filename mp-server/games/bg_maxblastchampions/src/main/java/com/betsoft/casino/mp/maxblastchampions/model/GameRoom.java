package com.betsoft.casino.mp.maxblastchampions.model;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.IGameState;
import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.mp.model.room.IMultiNodeRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.abs.casino.common.exception.CommonException;
import com.hazelcast.core.IExecutorService;

import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

@SuppressWarnings({"rawtypes"})
public class GameRoom extends BattleAbstractCrashGameRoom {
    public GameRoom(ApplicationContext context, Logger logger, IMultiNodeRoomInfo roomInfo, GameMap map, IPlayerStatsService playerStatsService,
                    IWeaponService weaponService, IExecutorService remoteExecutorService, IPlayerQuestsService playerQuestsService,
                    IPlayerProfileService playerProfileService, IGameConfigService gameConfigService, IActiveFrbSessionService activeFrbSessionService,
                    IActiveCashBonusSessionService activeCashBonusSessionService, ITournamentService tournamentService,
                    IGameConfigProvider gameConfigProvider, ISpawnConfigProvider spawnConfigProvider) {

        super(context, logger, roomInfo, map, playerStatsService, weaponService, remoteExecutorService, playerQuestsService,
                playerProfileService, gameConfigService, activeFrbSessionService, activeCashBonusSessionService, tournamentService,
                gameConfigProvider, spawnConfigProvider);
    }

    public GameRoom(ApplicationContext context, Logger logger, IMultiNodeRoomInfo roomInfo, IPlayerStatsService playerStatsService,
                    IWeaponService weaponService, IExecutorService remoteExecutorService, GameRoomSnapshot snapshot,
                    IPlayerQuestsService playerQuestsService, IPlayerProfileService playerProfileService, IGameConfigService gameConfigService,
                    IActiveFrbSessionService activeFrbSessionService, IActiveCashBonusSessionService activeCashBonusSessionService,
                    ITournamentService tournamentService, IGameConfigProvider gameConfigProvider, ISpawnConfigProvider spawnConfigProvider) {

        super(context, logger, roomInfo, playerStatsService, weaponService, remoteExecutorService, snapshot, playerQuestsService,
                playerProfileService, gameConfigService, activeFrbSessionService, activeCashBonusSessionService, tournamentService,
                gameConfigProvider, spawnConfigProvider);
    }

    @Override
    public GameType getGameType() {
        return GameType.BG_MAXCRASHGAME;
    }

    @Override
    public long getLockTimeout() {
        return 5000L;
    }

    @Override
    protected void initGameState(IGameState newState) throws CommonException {
        if (RoomState.PLAY.equals(newState.getRoomState())) {
            // first init, then change game state
            newState.init();
            this.gameState = newState;
        } else {
            super.initGameState(newState);
        }
    }

    @Override
    protected void notifyOnGameStateChanged(IGameState newState,
                                            RoomState oldRoomState,
                                            long timeToNextState,
                                            long roundId) {

        RoomState newRoomState = newState.getRoomState();
        super.notifyOnGameStateChanged(newState, oldRoomState, timeToNextState, roundId);

        if (RoomState.PLAY.equals(newRoomState)) {
            sendGameInfo();
        }
    }

    private void sendGameInfo() {
        for (Map.Entry<Long, IGameSocketClient> entry : observePlayers.entrySet()) {
            IGameSocketClient client = entry.getValue();
            sendGameInfoToSocketClient(client);
        }
    }
}
