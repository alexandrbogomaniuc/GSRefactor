package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class BetLevel extends TInboundObject {
    private int betLevel;

    public BetLevel(long date, int rid, int betLevel) {
        super(date, rid);
        this.betLevel = betLevel;
    }

    public int getBetLevel() {
        return betLevel;
    }

    public void setBetLevel(int betLevel) {
        this.betLevel = betLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BetLevel buyIn = (BetLevel) o;

        return betLevel == buyIn.betLevel;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + betLevel;
        return result;
    }

    @Override
    public String toString() {
        return "BetLevel[" +
                "betLevel=" + betLevel +
                ", rid=" + rid +
                ", date=" + date +
                ']';
    }
}
