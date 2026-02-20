package com.betsoft.casino.mp.amazon.model;

import java.io.IOException;

import com.betsoft.casino.mp.common.AbstractQualifyGameState;
import com.dgphoenix.casino.common.exception.CommonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * User: flsh
 * Date: 15.11.17.
 */
public class QualifyGameState extends AbstractQualifyGameState<GameRoom, Seat, GameMap, QualifyGameState> {
    public QualifyGameState() {
        super();
    }

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
