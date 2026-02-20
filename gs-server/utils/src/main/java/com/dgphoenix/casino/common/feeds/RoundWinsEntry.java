package com.dgphoenix.casino.common.feeds;

public class RoundWinsEntry {

    private long bet;
    private long win;

    public RoundWinsEntry(long bet, long win) {
        this.bet = bet;
        this.win = win;
    }

    public long getBet() {
        return bet;
    }

    public long getWin() {
        return win;
    }
}
