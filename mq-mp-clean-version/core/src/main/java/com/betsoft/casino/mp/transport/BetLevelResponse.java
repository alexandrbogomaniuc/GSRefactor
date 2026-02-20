package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class BetLevelResponse extends TInboundObject {
    private int seatId;
    private int betLevel;

    public BetLevelResponse(long date, int rid, int betLevel, int seatId) {
        super(date, rid);
        this.betLevel = betLevel;
        this.seatId = seatId;
    }

    public int getBetLevel() {
        return betLevel;
    }

    public void setBetLevel(int betLevel) {
        this.betLevel = betLevel;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BetLevelResponse that = (BetLevelResponse) o;
        return betLevel == that.betLevel;

    }

    @Override
    public String toString() {
        return "BetLevelResponse[" +
                "betLevel=" + betLevel +
                ", betLevel=" + betLevel +
                ", seatId=" + seatId +
                ", rid=" + rid +
                ", date=" + date +
                ']';
    }
}
