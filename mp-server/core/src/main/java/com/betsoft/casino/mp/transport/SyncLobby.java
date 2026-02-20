package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class SyncLobby extends TInboundObject {

    public SyncLobby(long date, int rid) {
        super(date, rid);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SyncLobby [");
        sb.append("inboundDate=").append(inboundDate);
        sb.append(", date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(']');
        return sb.toString();
    }
}
