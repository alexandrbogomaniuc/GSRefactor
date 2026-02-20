package com.betsoft.casino.mp.maxblastchampions.model;

public class PrivateBTGWaitingGameState extends WaitingPlayersGameState {
    public PrivateBTGWaitingGameState() {
        super();
    }

    public PrivateBTGWaitingGameState(BattleAbstractCrashGameRoom gameRoom) {
        super(gameRoom);
    }

    @Override
    protected long getStartNewRoundPause() {
        //to no get negative values for the further calculations
        //1 year = 31556952000ms
        return Long.MAX_VALUE - System.currentTimeMillis() - 31556952000L;
    }

    @Override
    protected PrivateBTGWaitingGameState getDeserializer() {
        return this;
    }
}
