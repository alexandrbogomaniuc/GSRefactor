package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

import java.util.StringJoiner;

public class CrashCancelAllBets extends TInboundObject {

    public CrashCancelAllBets(long date, int rid) {
        super(date, rid);
    }

    public int getCancelBetLimit() {
        return 2500;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrashCancelAllBets.class.getSimpleName() + "[", "]")
                .add("date=" + date)
                .add("rid=" + rid)
                .toString();
    }
}
