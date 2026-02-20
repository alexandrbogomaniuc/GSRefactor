package com.betsoft.casino.mp.maxcrashgame.model;

import com.betsoft.casino.mp.common.AbstractClosedGameState;

public class ClosedGameState extends AbstractClosedGameState<AbstractCrashGameRoom, Seat, GameMap, ClosedGameState> {
    public ClosedGameState() {}

    public ClosedGameState(AbstractCrashGameRoom gameRoom) {
        super(gameRoom);
    }

    @Override
    protected ClosedGameState getDeserializer() {
        return this;
    }
}
