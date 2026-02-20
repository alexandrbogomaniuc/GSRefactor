package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

public class LobbyTimeUpdated extends TObject {

    public LobbyTimeUpdated(long date, int rid) {
        super(date, rid);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LobbyTimeUpdated [");
        sb.append("date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(']');
        return sb.toString();
    }
}
