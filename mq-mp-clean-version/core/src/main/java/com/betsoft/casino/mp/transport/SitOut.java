package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ISitOut;
import com.betsoft.casino.utils.TInboundObject;

/**
 * User: flsh
 * Date: 03.06.17.
 */
public class SitOut extends TInboundObject implements ISitOut {

    public SitOut(long date, int rid) {
        super(date, rid);
    }

    @Override
    public String toString() {
        return "SitOut[" +
                "date=" + date +
                ", rid=" + rid +
                ']';
    }
}
