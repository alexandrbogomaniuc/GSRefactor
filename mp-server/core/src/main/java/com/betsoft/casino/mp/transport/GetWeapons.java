package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class GetWeapons extends TInboundObject {

    public GetWeapons(long date, int rid) {
        super(date, rid);
    }

    @Override
    public String toString() {
        return "GetWeapons[" +
                "date=" + date +
                ", rid=" + rid +
                ']';
    }
}
