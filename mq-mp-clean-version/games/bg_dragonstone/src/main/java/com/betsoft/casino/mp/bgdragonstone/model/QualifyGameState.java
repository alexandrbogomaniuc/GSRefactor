package com.betsoft.casino.mp.bgdragonstone.model;

import com.betsoft.casino.mp.common.AbstractQualifyGameState;
import com.betsoft.casino.mp.model.IGameState;
import com.dgphoenix.casino.common.exception.CommonException;

public class QualifyGameState extends AbstractQualifyGameState<GameRoom, Seat, GameMap, QualifyGameState> {

    public QualifyGameState() {}

    public QualifyGameState(GameRoom gameRoom, int lastUsedMapId, int pauseTime, long roundTimeStart, long roundTimeEnd) {
        super(gameRoom, lastUsedMapId, pauseTime, roundTimeStart, roundTimeEnd);
        if (gameRoom != null) { // may be null on serialization
            gameRoom.getSeats().forEach(seat -> skipResults.put(seat.getNumber(), false));
        }
    }

    @Override
    public int getQualifyPauseTime() {
        return 5000;
    }

    @Override
    public void setWaitingPlayersGameState() throws CommonException {
        IGameState waitingState = gameRoom.getWaitingPlayersGameState();
        gameRoom.setGameState(waitingState);
    }

    public boolean isReconnectAllowed() {
        return false;
    }

    @Override
    protected QualifyGameState getDeserializer() {
        return this;
    }

}
