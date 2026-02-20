package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.IGameState;
import com.betsoft.casino.mp.model.IMap;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.ISharedGameStateService;

public abstract class AbstractMultiNodeWaitingPlayerGameState<GAMEROOM extends AbstractMultiNodeGameRoom, SEAT extends ISeat, MAP extends IMap, GS extends IGameState>
        extends AbstractWaitingPlayersGameState<GAMEROOM, SEAT, MAP, GS> {

    public AbstractMultiNodeWaitingPlayerGameState() {
        super();
    }

    public AbstractMultiNodeWaitingPlayerGameState(GAMEROOM gameRoom) {
        super(gameRoom);
    }

    @Override
    protected boolean isNeedStartNewRound() {

        ISharedGameStateService sharedGameStateService = this.getGameStateService();
        if(sharedGameStateService != null) {
            SharedCrashGameState sharedCrashGameState = sharedGameStateService.get(getRoomId(), SharedCrashGameState.class);
            if(sharedCrashGameState != null) {
                return sharedCrashGameState.isNeedStartNewRound();
            }
        }

        return false;
    }

    @Override
    protected void setNeedStartNewRound(boolean needStartNewRound) {
        ISharedGameStateService sharedGameStateService = this.getGameStateService();
        if(sharedGameStateService != null) {
            SharedCrashGameState sharedCrashGameState = sharedGameStateService.get(getRoomId(), SharedCrashGameState.class);
            if(sharedCrashGameState != null) {
                sharedCrashGameState.setNeedStartNewRound(needStartNewRound);
                getGameStateService().put(sharedCrashGameState);
            }
        }
    }

    protected ISharedGameStateService getGameStateService() {
        return gameRoom.getSharedGameStateService();
    }
}
