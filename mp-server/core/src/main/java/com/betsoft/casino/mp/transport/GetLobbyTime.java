package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class GetLobbyTime extends TInboundObject {

    public GetLobbyTime(long date, int rid) {
        super(date, rid);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GetLobbyTime [");
        sb.append("date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(']');
        return sb.toString();
    }
}
