package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.room.IPendingOperationStatus;
import com.betsoft.casino.utils.TInboundObject;

public class PendingOperationStatus extends TInboundObject implements IPendingOperationStatus {
    private boolean pending;

    public PendingOperationStatus(long date, int rid, boolean pending) {
        super(date, rid);
        this.pending = pending;
    }

    public boolean getPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    @Override
    public String toString() {
        return "PendingOperationStatus{" +
                "pending=" + pending +
                ", inboundDate=" + inboundDate +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }
}
