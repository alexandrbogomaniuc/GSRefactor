package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class CrashBets extends TInboundObject {
    private List<CrashBet> bets;

    public CrashBets(long date, int rid, List<CrashBet> bets) {
        super(date, rid);
        this.bets = bets;
    }

    public List<CrashBet> getBets() {
        return bets;
    }

    @Override
    public int getFrequencyLimit() {
        return 50;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CrashBets crashBets = (CrashBets) o;
        return Objects.equals(bets, crashBets.bets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bets);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrashBets.class.getSimpleName() + "[", "]")
                .add("date=" + date)
                .add("rid=" + rid)
                .add("bets=" + bets)
                .toString();
    }
}
