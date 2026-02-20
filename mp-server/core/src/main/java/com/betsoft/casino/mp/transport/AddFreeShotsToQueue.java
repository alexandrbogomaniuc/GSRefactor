package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IAddFreeShotsToQueue;
import com.betsoft.casino.utils.TInboundObject;

import java.util.Objects;

public class AddFreeShotsToQueue extends TInboundObject implements IAddFreeShotsToQueue {
    private String queue;

    public AddFreeShotsToQueue(long date, int rid, String queue) {
        super(date, rid);
        this.queue = queue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AddFreeShotsToQueue that = (AddFreeShotsToQueue) o;
        return Objects.equals(queue, that.queue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), queue);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddFreeShotsToQueue{");
        sb.append("queue='").append(queue).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String getQueue() {
        return queue;
    }

    @Override
    public void setQueue(String queue) {
        this.queue = queue;
    }
}


