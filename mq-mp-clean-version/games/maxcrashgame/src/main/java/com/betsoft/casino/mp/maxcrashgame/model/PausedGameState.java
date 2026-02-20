package com.betsoft.casino.mp.maxcrashgame.model;

import com.betsoft.casino.mp.common.AbstractPausedGameState;
import com.betsoft.casino.mp.model.IGameState;

public class PausedGameState extends AbstractPausedGameState<AbstractCrashGameRoom, Seat, GameMap, PausedGameState> {

    public static final int PAUSE_STATE_DURATION = 3000;

    public PausedGameState() {}

    public PausedGameState(AbstractCrashGameRoom gameRoom, IGameState<AbstractCrashGameRoom, Seat, GameMap, ?> nextState) {
        super(gameRoom, nextState);
    }

    @Override
    public int getPauseTime() {
        return PAUSE_STATE_DURATION;
    }

    @Override
    public long getTimeToNextState() {
        return gameRoom.isTimerStopped() ?
                PAUSE_STATE_DURATION :
                gameRoom.getTimerRemaining();

    }

    @Override
    protected PausedGameState getDeserializer() {
        return this;
    }

}
