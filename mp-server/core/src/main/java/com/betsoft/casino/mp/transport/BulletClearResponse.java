package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IBulletClearResponse;
import com.betsoft.casino.utils.TInboundObject;

public class BulletClearResponse extends TInboundObject implements IBulletClearResponse {
    private int seatNumber;

    public BulletClearResponse(long date, int rid, int seatNumber) {
        super(date, rid);
        this.seatNumber = seatNumber;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    @Override
    public String toString() {
        return "BulletClearResponse[" +
                "seatNumber=" + seatNumber +
                ", rid=" + rid +
                ", date=" + date +
                ']';
    }
}

