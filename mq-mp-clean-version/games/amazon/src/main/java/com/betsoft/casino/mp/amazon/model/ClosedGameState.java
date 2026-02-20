package com.betsoft.casino.mp.amazon.model;

import com.betsoft.casino.mp.common.AbstractClosedGameState;

/**
 * User: flsh
 * Date: 15.11.17.
 */
public class ClosedGameState extends AbstractClosedGameState<GameRoom, Seat, GameMap, ClosedGameState> {
    public ClosedGameState() {}

    public ClosedGameState(GameRoom gameRoom) {
        super(gameRoom);
    }

    @Override
    protected ClosedGameState getDeserializer() {
        return this;
    }
}
