package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class BulletClear extends TInboundObject {

    public BulletClear(long date, int rid) {
        super(date, rid);
    }

    @Override
    public String toString() {
        return "BulletClear[" +
                ", rid=" + rid +
                ", date=" + date +
                ']';
    }
}
