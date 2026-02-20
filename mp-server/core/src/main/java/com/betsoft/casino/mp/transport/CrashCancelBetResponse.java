package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ICrashCancelBet;
import com.betsoft.casino.utils.TObject;

import java.util.StringJoiner;

public class CrashCancelBetResponse extends TObject implements ICrashCancelBet {
    private double currentMult;
    private int seatId;
    private long seatWin;
    private String crashBetId;
    private String name;
    private Long balance;

    public CrashCancelBetResponse(long date, int rid, double currentMult, int seatId, long seatWin, String crashBetId, String name) {
        super(date, rid);
        this.currentMult = currentMult;
        this.seatId = seatId;
        this.seatWin = seatWin;
        this.crashBetId = crashBetId;
        this.name = name;
    }

    public CrashCancelBetResponse(long date, int rid, double currentMult, int seatId, long seatWin, String crashBetId, String name, Long balance) {
        super(date, rid);
        this.currentMult = currentMult;
        this.seatId = seatId;
        this.seatWin = seatWin;
        this.crashBetId = crashBetId;
        this.name = name;
        this.balance = balance;
    }

    @Override
    public double getCurrentMult() {
        return currentMult;
    }

    public void setCurrentMult(double currentMult) {
        this.currentMult = currentMult;
    }

    @Override
    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    @Override
    public long getSeatWin() {
        return seatWin;
    }

    public void setSeatWin(long seatWin) {
        this.seatWin = seatWin;
    }

    @Override
    public String getCrashBetId() {
        return crashBetId;
    }

    public void setCrashBetId(String crashBetId) {
        this.crashBetId = crashBetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrashCancelBetResponse.class.getSimpleName() + "[", "]")
                .add("date=" + date)
                .add("rid=" + rid)
                .add("currentMult=" + currentMult)
                .add("seatId=" + seatId)
                .add("seatWin=" + seatWin)
                .add("crashBetId='" + crashBetId + "'")
                .add("name='" + name + "'")
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CrashCancelBetResponse that = (CrashCancelBetResponse) o;

        if (Double.compare(that.currentMult, currentMult) != 0) return false;
        if (!crashBetId.equals(that.crashBetId)) return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(currentMult);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + crashBetId.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}

