package com.betsoft.casino.mp.bgdragonstone.model;

import com.betsoft.casino.mp.common.AbstractPausedGameState;
import com.betsoft.casino.mp.model.IGameState;

public class PausedGameState extends AbstractPausedGameState<GameRoom, Seat, GameMap, PausedGameState> {

    public PausedGameState(GameRoom gameRoom, IGameState nextState) {
        super(gameRoom, nextState);
    }

    @Override
    public int getPauseTime() {
        return 4000;
    }

    @Override
    protected PausedGameState getDeserializer() {
        return this;
    }
}
