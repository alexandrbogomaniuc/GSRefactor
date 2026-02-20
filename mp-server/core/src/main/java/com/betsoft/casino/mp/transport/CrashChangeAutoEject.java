package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

import java.util.StringJoiner;

/**
 * User: flsh
 * Date: 24.03.2022.
 */
public class CrashChangeAutoEject extends TInboundObject {
    private String betId;
    private double multiplier;

    public CrashChangeAutoEject(long date, int rid, String betId) {
        super(date, rid);
        this.betId = betId;
    }

    public String getBetId() {
        return betId;
    }

    public void setBetId(String betId) {
        this.betId = betId;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CrashChangeAutoEject that = (CrashChangeAutoEject) o;

        if (Double.compare(that.multiplier, multiplier) != 0) return false;
        return betId.equals(that.betId);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + betId.hashCode();
        temp = Double.doubleToLongBits(multiplier);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrashChangeAutoEject.class.getSimpleName() + "[", "]")
                .add("date=" + date)
                .add("rid=" + rid)
                .add("betId='" + betId + "'")
                .add("multiplier=" + multiplier)
                .toString();
    }

}
