package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class CollectQuest extends TInboundObject {
    private long id;

    public CollectQuest(long date, int rid, long id) {
        super(date, rid);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "GetBalance[" +
                "date=" + date +
                ", rid=" + rid +
                ", id=" + id +
                ']';
    }
}

