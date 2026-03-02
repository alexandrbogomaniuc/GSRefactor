package com.betsoft.casino.mp.revengeofra.model;

import com.betsoft.casino.mp.common.AbstractQualifyGameState;
import com.abs.casino.common.exception.CommonException;

public class QualifyGameState extends AbstractQualifyGameState<GameRoom, Seat, GameMap, QualifyGameState> {
    public QualifyGameState() {}

    public QualifyGameState(GameRoom gameRoom, int lastUsedMapId, int pauseTime, long roundTimeStart, long roundTimeEnd) {
        super(gameRoom, lastUsedMapId, pauseTime, roundTimeStart, roundTimeEnd);
        if (gameRoom != null) { // may be null on serialization
            gameRoom.getSeats().forEach(seat -> skipResults.put(seat.getNumber(), false));
        }
    }

    @Override
    public void setWaitingPlayersGameState() throws CommonException {
        WaitingPlayersGameState newGameState = new WaitingPlayersGameState(gameRoom);
        gameRoom.setGameState(newGameState);
    }

    @Override
    protected QualifyGameState getDeserializer() {
        return this;
    }

}
