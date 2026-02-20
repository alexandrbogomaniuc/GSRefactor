package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class GetBalance extends TInboundObject {

    public GetBalance(long date, int rid) {
        super(date, rid);
    }

    @Override
    public String toString() {
        return "GetBalance[" +
                "date=" + date +
                ", rid=" + rid +
                ']';
    }
}
