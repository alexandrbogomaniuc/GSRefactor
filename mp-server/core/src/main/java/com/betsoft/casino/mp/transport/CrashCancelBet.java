package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

import java.util.Objects;

public class CrashCancelBet extends TInboundObject {
    private String crashBetId;
    private boolean placeNewBet;

    public CrashCancelBet(long date, int rid, String crashBetId) {
        super(date, rid);
        this.crashBetId = crashBetId;
    }

    public String getCrashBetId() {
        return crashBetId;
    }

    public void setCrashBetId(String crashBetId) {
        this.crashBetId = crashBetId;
    }

    public boolean isPlaceNewBet() {
        return placeNewBet;
    }

    public void setPlaceNewBet(boolean placeNewBet) {
        this.placeNewBet = placeNewBet;
    }

    @Override
    public int getFrequencyLimit() {
        return 50;
    }

    public int getCancelBetLimit() {
        return 500;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CrashCancelBet that = (CrashCancelBet) o;

        return Objects.equals(crashBetId, that.crashBetId);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (crashBetId != null ? crashBetId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CrashCancelBet[" +
                "crashBetId=" + crashBetId +
                ", placeNewBet=" + placeNewBet +
                ", rid=" + rid +
                ", date=" + date +
                ']';
    }
}
