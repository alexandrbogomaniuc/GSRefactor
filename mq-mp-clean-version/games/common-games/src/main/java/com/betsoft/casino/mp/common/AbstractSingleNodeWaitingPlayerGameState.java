package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.IGameState;
import com.betsoft.casino.mp.model.IMap;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.room.IRoom;

/**
 * User: flsh
 * Date: 09.12.2022.
 */
public abstract class AbstractSingleNodeWaitingPlayerGameState<GAMEROOM extends IRoom, SEAT extends ISeat, MAP extends IMap, GS extends IGameState>
        extends AbstractWaitingPlayersGameState<GAMEROOM, SEAT, MAP, GS> {
    protected volatile boolean needStartNewRound;

    public AbstractSingleNodeWaitingPlayerGameState() {
        super();
    }

    public AbstractSingleNodeWaitingPlayerGameState(GAMEROOM gameRoom) {
        super(gameRoom);
    }

    @Override
    protected boolean isNeedStartNewRound() {
        return needStartNewRound;
    }

    @Override
    protected void setNeedStartNewRound(boolean needStartNewRound) {
        this.needStartNewRound = needStartNewRound;
    }
}
