package com.abs.casino.common.cache.data;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.dgphoenix.casino.common.cache.Identifiable;

public class IdObject implements IDistributedCacheEntry, Identifiable, IDistributedConfigEntry {
    protected long id;

    public IdObject() {

    }

    public IdObject(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean equals(Object obj) {
        if (obj instanceof IdObject) {
            return id == ((IdObject) obj).getId();
        }
        return false;
    }

    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public void copy(IDistributedConfigEntry entry) {
        this.id = ((IdObject) entry).id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IdObject [");
        sb.append("id=").append(id);
        sb.append(']');
        return sb.toString();
    }
}
