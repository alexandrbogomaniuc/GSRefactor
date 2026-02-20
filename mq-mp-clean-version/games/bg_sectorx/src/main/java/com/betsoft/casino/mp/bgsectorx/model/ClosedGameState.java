package com.betsoft.casino.mp.bgsectorx.model;

import com.betsoft.casino.mp.common.AbstractClosedGameState;

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
