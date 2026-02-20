package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class ReBuy extends TInboundObject {

    public ReBuy(long date, int rid) {
        super(date, rid);
    }

    @Override
    public String toString() {
        return "ReBuy{" +
                "date=" + date +
                ", rid=" + rid +
                '}';
    }
}
