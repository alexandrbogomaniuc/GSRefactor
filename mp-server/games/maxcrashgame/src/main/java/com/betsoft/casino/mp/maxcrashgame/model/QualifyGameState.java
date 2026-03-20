package com.betsoft.casino.mp.maxcrashgame.model;

import com.betsoft.casino.mp.common.AbstractQualifyGameState;
import com.betsoft.casino.mp.common.SharedCrashGameState;
import com.betsoft.casino.mp.model.ISharedGameStateService;
import com.betsoft.casino.mp.model.PlaySubround;
import com.betsoft.casino.mp.model.RoomState;
import com.abs.casino.common.exception.CommonException;

public class QualifyGameState extends AbstractQualifyGameState<AbstractCrashGameRoom, Seat, GameMap, QualifyGameState> {
    public QualifyGameState() {}

    public QualifyGameState(AbstractCrashGameRoom gameRoom, int lastUsedMapId, int pauseTime, long roundTimeStart, long roundTimeEnd) {
        super(gameRoom, lastUsedMapId, pauseTime, roundTimeStart, roundTimeEnd);
    }

    @Override
    public int getQualifyPauseTime() {
        return 0;
    }

    @Override
    public void setWaitingPlayersGameState() throws CommonException {
        WaitingPlayersGameState newGameState = new WaitingPlayersGameState(gameRoom);
        gameRoom.setGameState(newGameState);
    }

    @Override
    public void init() throws CommonException {
        gameRoom.sendChanges(gameRoom.getTOFactoryService().createChangeMap(getCurrentTime(), gameRoom.getNextMapId(),
                PlaySubround.BASE.name()));
        gameRoom.lock();
        try {
            if (!getRoomInfo().isBattlegroundMode()) {
                gameRoom.updateStats();
            }
            ISharedGameStateService sharedGameStateService = gameRoom.getSharedGameStateService();
            SharedCrashGameState crashGameState = sharedGameStateService.get(gameRoom.getId(), SharedCrashGameState.class);
            if (crashGameState != null) {
                crashGameState.setState(RoomState.QUALIFY);
                sharedGameStateService.put(crashGameState);
            }
            gameRoom.sendRoundResults();
            gameRoom.resetRoundResults();
            gameRoom.convertBulletsToMoney();
            gameRoom.getMap().removeAllEnemies();
            gameRoom.resetRoundResults();
        } catch (Exception e) {
            getLog().error("QualifyGameState: init error", e);
        } finally {
            try {
                gameRoom.setTimerTime(getPauseTime());
                gameRoom.startTimer();
            } finally {
                gameRoom.unlock();
            }
        }
    }

    @Override
    protected QualifyGameState getDeserializer() {
        return this;
    }
}
