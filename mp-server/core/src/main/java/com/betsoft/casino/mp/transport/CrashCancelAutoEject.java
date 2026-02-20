package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

import java.util.StringJoiner;

/**
 * User: flsh
 * Date: 09.03.2022.
 */
public class CrashCancelAutoEject extends TInboundObject {
    private String betId;

    public CrashCancelAutoEject(long date, int rid, String betId) {
        super(date, rid);
        this.betId = betId;
    }

    public String getBetId() {
        return betId;
    }

    public void setBetId(String betId) {
        this.betId = betId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CrashCancelAutoEject that = (CrashCancelAutoEject) o;

        return betId.equals(that.betId);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + betId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrashCancelAutoEject.class.getSimpleName() + "[", "]")
                .add("date=" + date)
                .add("rid=" + rid)
                .add("betId='" + betId + "'")
                .toString();
    }

}
