package com.betsoft.casino.mp;

import com.betsoft.casino.mp.model.IActionGameSeat;
import com.betsoft.casino.mp.model.IBattlegroundRoom;
import com.betsoft.casino.mp.model.IGameState;
import com.betsoft.casino.mp.model.IMap;
import com.betsoft.casino.mp.model.RoomState;
import com.dgphoenix.casino.common.exception.CommonException;

public abstract class AbstractPrivateBTGWaitingGameState<GAMEROOM extends IBattlegroundRoom, SEAT extends IActionGameSeat, MAP extends IMap, GS extends IGameState>
        extends AbstractBattlegroundWaitingPlayersGameState<GAMEROOM, SEAT, MAP, GS> {

    public AbstractPrivateBTGWaitingGameState() {
        super();
    }

    public AbstractPrivateBTGWaitingGameState(GAMEROOM gameRoom) {
        super(gameRoom);
    }

    public void onTimer(boolean needClearEnemies) throws CommonException {
        gameRoom.lock();
        try {
            short seatsCount = gameRoom.getRealConfirmedSeatsCount();
            boolean timeIsOver = System.currentTimeMillis() >= timeToStart;
            boolean readyStartRound = seatsCount >= gameRoom.getMinSeats() && timeIsOver;
            gameRoom.updateRoomInfoForDeactivation();
            boolean isNeedDeactivate = gameRoom.getRoomInfo().isDeactivated();
            if (isNeedDeactivate) {
                gameRoom.removePlayersFromPrivateRoom();
            } else {
                if (readyStartRound) {
                    gameRoom.removeSeatsWithPendingOperations();
                    gameRoom.sitOutAllPlayersWithoutConfirmedRebuy();
                    setPlayGameState();
                } else {
                    if (timeIsOver) {
                        getLog().debug("onTimer, found case for reBuy failed, need cancel BG and return money " +
                                "seatsCount={}, timeToStart={}", seatsCount, timeToStart);
                        gameRoom.removeSeatsWithPendingOperations();
                        gameRoom.convertBulletsToMoney();
                        //cancel BG and return all money
                    } else {
                        gameRoom.sitOutAllDisconnectedPlayers();
                    }
                    gameRoom.stopTimer();
                    gameRoom.setTimerTime(START_NEW_ROUND_PAUSE_FOR_BG);
                    gameRoom.startTimer();
                }
            }
        } finally {
            gameRoom.unlock();
        }
    }

    @Override
    public void firePlayersCountChanged() throws CommonException {
        gameRoom.lock();
        try {

            getLog().debug("firePlayersCountChanged: (AbstractPrivate): Room State={}", gameRoom.getState());

            if (gameRoom.getState().equals(RoomState.WAIT)) {
                short realSeatsCount = gameRoom.getRealConfirmedSeatsCount();
                getLog().debug("firePlayersCountChanged: (AbstractPrivate): realSeatsCount={}, minSeats={}, needStartNewRound={} " +
                                ", timeToStart before: {}",
                        realSeatsCount, gameRoom.getMinSeats(), needStartNewRound, timeToStart);
                if ((realSeatsCount == 0 && timeToStart < Long.MAX_VALUE)) {
                    timeToStart = Long.MAX_VALUE;
                    getLog().debug("firePlayersCountChanged: (AbstractPrivate): reset timeToStart: {} ", timeToStart);
                }
                if (!gameRoom.isTimerStopped()) {
                    getLog().debug("firePlayersCountChanged: (AbstractPrivate): stopTimer");
                    gameRoom.stopTimer();
                }
                gameRoom.setTimerTime(START_NEW_ROUND_PAUSE_FOR_BG);
                gameRoom.startTimer();
                getLog().debug("firePlayersCountChanged: (AbstractPrivate): startTimer");

            } else {
                getLog().debug("firePlayersCountChanged: (AbstractPrivate): game room is in PLAY state, not need any action");
            }
        } finally {
            gameRoom.unlock();
        }
    }
}
