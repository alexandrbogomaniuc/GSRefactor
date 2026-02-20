package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.room.IKickResponse;
import com.betsoft.casino.utils.TInboundObject;

public class KickResponse extends TInboundObject implements IKickResponse {

    public KickResponse(long date, int rid) {
        super(date, rid);
    }

    @Override
    public String toString() {
        return "KickResponse{" +
                "date=" + date +
                ", rid=" + rid +
                '}';
    }
}
