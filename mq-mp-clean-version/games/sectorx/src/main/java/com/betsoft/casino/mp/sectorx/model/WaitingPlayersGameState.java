package com.betsoft.casino.mp.sectorx.model;

import com.betsoft.casino.mp.common.AbstractSingleNodeWaitingPlayerGameState;
import com.dgphoenix.casino.common.exception.CommonException;

public class WaitingPlayersGameState extends AbstractSingleNodeWaitingPlayerGameState<GameRoom, Seat, GameMap, WaitingPlayersGameState> {
    public WaitingPlayersGameState() {}

    public WaitingPlayersGameState(GameRoom gameRoom) {
        super(gameRoom);
    }

    @Override
    protected void setPlayGameState() throws CommonException {
        gameRoom.setGameState(new PlayGameState(gameRoom));
    }

    @Override
    protected WaitingPlayersGameState getDeserializer() {
        return this;
    }
}
