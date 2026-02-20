package com.betsoft.casino.mp.bgdragonstone.model;

import com.betsoft.casino.mp.AbstractBattlegroundWaitingPlayersGameState;
import com.dgphoenix.casino.common.exception.CommonException;

public class WaitingPlayersGameState extends AbstractBattlegroundWaitingPlayersGameState<GameRoom, Seat, GameMap, WaitingPlayersGameState> {
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
