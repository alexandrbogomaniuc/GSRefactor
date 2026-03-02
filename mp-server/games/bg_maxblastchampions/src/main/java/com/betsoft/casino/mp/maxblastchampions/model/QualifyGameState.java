package com.betsoft.casino.mp.maxblastchampions.model;

import com.betsoft.casino.mp.common.AbstractQualifyGameState;
import com.betsoft.casino.mp.common.SharedCrashGameState;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.abs.casino.common.exception.CommonException;

import java.util.Map;
import java.util.concurrent.Executors;

public class QualifyGameState extends AbstractQualifyGameState<BattleAbstractCrashGameRoom, Seat, GameMap, QualifyGameState> {

    public QualifyGameState() {}

    public QualifyGameState(BattleAbstractCrashGameRoom gameRoom, int lastUsedMapId, int pauseTime, long roundTimeStart, long roundTimeEnd) {
        super(gameRoom, lastUsedMapId, pauseTime, roundTimeStart, roundTimeEnd);
    }

    @Override
    public int getQualifyPauseTime() {
        return 0;
    }

    @Override
    public void setWaitingPlayersGameState() throws CommonException {

        IGameState waitingPlayersGameState = gameRoom.getWaitingPlayersGameState();
        gameRoom.setGameState(waitingPlayersGameState);

    }

    @Override
    public void init() throws CommonException {

        IChangeMap changeMap = gameRoom.getTOFactoryService().createChangeMap(
                getCurrentTime(),
                gameRoom.getNextMapId(),
                PlaySubround.BASE.name()
        );

        gameRoom.sendChanges(changeMap);

        gameRoom.lock();

        try {

            if (!getRoomInfo().isBattlegroundMode()) {
                gameRoom.updateStats();
            }

            ISharedGameStateService sharedGameStateService = gameRoom.getSharedGameStateService();

            SharedCrashGameState sharedCrashGameState =
                    sharedGameStateService.get(gameRoom.getId(), SharedCrashGameState.class);

            if (sharedCrashGameState != null) {

                boolean qualifyRoundResultFinished = sharedCrashGameState.isQualifyRoundResultFinished();
                getLog().debug("init: qualifyRoundResultFinished: {}", qualifyRoundResultFinished);

                if (!qualifyRoundResultFinished) {

                    Map<Long, IRoundResult> roundResults = gameRoom.getExtResultsAndSendForLocal();
                    getLog().debug("init: roundResults={}", roundResults);

                    if(roundResults != null &&  !roundResults.entrySet().isEmpty()) {

                        IRoundResult roundResultObserversNoSeat = null;
                        for (Map.Entry<Long, IRoundResult> entry : roundResults.entrySet()) {
                            roundResultObserversNoSeat = entry.getValue().copy();
                            break; // break after the first element
                        }

                        if(roundResultObserversNoSeat != null) {

                            roundResultObserversNoSeat.setWinAmount(0);
                            roundResultObserversNoSeat.setRealWinAmount(0);
                            roundResultObserversNoSeat.setWinRebuyAmount(0);

                            IRoundResult roundResult = roundResultObserversNoSeat.copy();

                            Executors.newSingleThreadExecutor().execute(() ->
                                    gameRoom.sendRoundResultForExtObserversWithNoSeats(roundResult)
                            );
                        }

                        getLog().debug("init: roundResultObserversNoSeat={},", roundResultObserversNoSeat);

                    }

                    sharedCrashGameState.setQualifyRoundResultFinished(true);
                    getLog().debug("init: save sharedCrashGameState: {}", sharedCrashGameState);

                    Executors.newSingleThreadExecutor().execute(() ->
                            gameRoom.sendRoundResultsForExtSeats(roundResults)
                    );
                }

                sharedCrashGameState.setState(RoomState.QUALIFY);

            } else {
                getLog().warn("init: sharedCrashGameState not found, create new");
                IRoomInfo roomInfo = getRoomInfo();

                sharedCrashGameState =
                        new SharedCrashGameState(RoomState.QUALIFY, getRoomId(), roomInfo.getRoundId(), 0, 0, null);

                sharedCrashGameState.setCalculationFinished(false);
                sharedCrashGameState.setRoundResultProcessingStarted(false);

            }

            sharedGameStateService.put(sharedCrashGameState);

            gameRoom.resetRoundResults();
            gameRoom.convertBulletsToMoney();

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
