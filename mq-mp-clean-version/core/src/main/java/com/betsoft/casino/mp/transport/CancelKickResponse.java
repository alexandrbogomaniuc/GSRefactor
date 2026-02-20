package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.room.ICancelKickResponse;
import com.betsoft.casino.utils.TInboundObject;

public class CancelKickResponse extends TInboundObject implements ICancelKickResponse {

    public CancelKickResponse(long date, int rid) {
        super(date, rid);
    }

    @Override
    public String toString() {
        return "CancelKickResponse{" +
                "date=" + date +
                ", rid=" + rid +
                '}';
    }
}
