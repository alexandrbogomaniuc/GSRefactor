package com.abs.casino.common.promo.messages.client.requests;

import com.dgphoenix.casino.common.cache.Identifiable;

/**
 * Created by vladislav on 12/20/16.
 */
public abstract class ClientRequest implements Identifiable {
    protected long id;

    protected ClientRequest() {
    }

    protected ClientRequest(long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientRequest that = (ClientRequest) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "id=" + id;
    }
}
