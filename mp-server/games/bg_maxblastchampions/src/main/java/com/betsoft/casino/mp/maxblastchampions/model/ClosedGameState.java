package com.betsoft.casino.mp.maxblastchampions.model;

import com.betsoft.casino.mp.common.AbstractClosedGameState;

public class ClosedGameState extends AbstractClosedGameState<BattleAbstractCrashGameRoom, Seat, GameMap, ClosedGameState> {
    public ClosedGameState() {}

    public ClosedGameState(BattleAbstractCrashGameRoom gameRoom) {
        super(gameRoom);
    }

    @Override
    protected ClosedGameState getDeserializer() {
        return this;
    }
}
