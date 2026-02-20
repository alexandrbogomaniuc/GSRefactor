package com.betsoft.casino.mp.pirates.model;

import com.betsoft.casino.mp.common.AbstractSingleNodeWaitingPlayerGameState;
import com.dgphoenix.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 14.11.17.
 */
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
