package com.abs.casino.common.remotecall;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.dgphoenix.casino.common.cache.Identifiable;
import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * User: flsh
 * Date: 02.04.13
 */
public class PersistableCall implements IDistributedCacheEntry, Identifiable, IRemoteCall, KryoSerializable,
        Comparable<PersistableCall>{
    private static final byte VERSION = 0;
    private long id;
    private IRemoteCall remoteCall;
    private long time;
    private int serverId;

    public PersistableCall() {
    }

    public PersistableCall(long id, IRemoteCall remoteCall, long time, int serverId) {
        this.id = id;
        if(remoteCall == null) {
            throw new IllegalArgumentException("remoteCall is null");
        }
        this.remoteCall = remoteCall;
        this.time = time;
        this.serverId = serverId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void call() throws CommonException {
        remoteCall.call();
    }

    public IRemoteCall getRemoteCall() {
        return remoteCall;
    }

    public void setRemoteCall(IRemoteCall remoteCall) {
        this.remoteCall = remoteCall;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeLong(time, true);
        output.writeInt(serverId, true);
        kryo.writeClassAndObject(output, remoteCall);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        id = input.readLong(true);
        time = input.readLong(true);
        serverId = input.readInt(true);
        remoteCall = (IRemoteCall) kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PersistableCall");
        sb.append("[id=").append(id);
        sb.append(", remoteCall=").append(remoteCall);
        sb.append(", time=").append(time);
        sb.append(", serverId=").append(serverId);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int compareTo(PersistableCall o) {
        long thisVal = this.id;
        long anotherVal = o.id;
        return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PersistableCall that = (PersistableCall) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
