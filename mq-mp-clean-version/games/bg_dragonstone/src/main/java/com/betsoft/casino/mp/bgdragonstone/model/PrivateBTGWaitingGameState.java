package com.betsoft.casino.mp.bgdragonstone.model;

import com.betsoft.casino.mp.AbstractPrivateBTGWaitingGameState;
import com.dgphoenix.casino.common.exception.CommonException;

public class PrivateBTGWaitingGameState extends AbstractPrivateBTGWaitingGameState<GameRoom, Seat, GameMap, PrivateBTGWaitingGameState> {
    public PrivateBTGWaitingGameState() {}

    public PrivateBTGWaitingGameState(GameRoom gameRoom) {
        super(gameRoom);
        timeToStart = Long.MAX_VALUE;
    }

    @Override
    protected void setPlayGameState() throws CommonException {
        gameRoom.setGameState(new PlayGameState(gameRoom));
    }

    @Override
    protected PrivateBTGWaitingGameState getDeserializer() {
        return this;
    }
}
