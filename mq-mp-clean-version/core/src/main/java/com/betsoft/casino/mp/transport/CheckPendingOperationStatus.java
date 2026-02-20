package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class CheckPendingOperationStatus extends TInboundObject {
    private String sid;

    public CheckPendingOperationStatus(long date, int rid, String sid) {
        super(date, rid);
        this.sid = sid;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    @Override
    public String toString() {
        return "CheckPendingOperationStatus{" +
                "sid=" + sid +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }
}
