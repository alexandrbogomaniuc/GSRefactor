package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class GetQuests extends TInboundObject {

    public GetQuests(long date, int rid) {
        super(date, rid);
    }

    @Override
    public String toString() {
        return "GetQuests[" +
                "date=" + date +
                ", rid=" + rid +
                ']';
    }
}
