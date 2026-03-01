package com.abs.casino.common.transport;

import com.abs.casino.common.transport.TObject;

/**
 * User: flsh
 * Date: 13.09.18.
 */
public abstract class TInboundObject extends TObject implements InboundObject {
    protected transient long inboundDate = System.currentTimeMillis();

    public TInboundObject() {}

    public TInboundObject(long date, int rid) {
        super(date, rid);
    }

    @Override
    public long getInboundDate() {
        return inboundDate;
    }

    @Override
    public void setInboundDate(long inboundDate) {
        this.inboundDate = inboundDate;
    }
}
