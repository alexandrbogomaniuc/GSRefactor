package com.abs.casino.common.cache.data.bonus;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.dgphoenix.casino.common.cache.Identifiable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author <a href="mailto:dader@dgphoenix.com">Timur Shaymardanov</a>
 * @since 05.03.2019
 */
public class DelayedMassAwardDelivery implements Identifiable, IDistributedCacheEntry, KryoSerializable {
    private static final byte VERSION = 1;

    private long id;
    private String errorDesc;
    private String accountIds;
    private boolean sentByAlert;

    public DelayedMassAwardDelivery() {}

    public DelayedMassAwardDelivery(long id, String errorDesc, String accountIds) {
        this.id = id;
        this.errorDesc = errorDesc;
        this.accountIds = accountIds;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }

    public String getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(String accountIds) {
        this.accountIds = accountIds;
    }

    public boolean isSentByAlert() {
        return sentByAlert;
    }

    public void setSentByAlert(boolean sentByAlert) {
        this.sentByAlert = sentByAlert;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DelayedMassAwardDelivery)) return false;
        DelayedMassAwardDelivery that = (DelayedMassAwardDelivery) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeString(errorDesc);
        output.writeString(accountIds);
        output.writeBoolean(sentByAlert);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        id = input.readLong(true);
        errorDesc = input.readString();
        accountIds = input.readString();
        if (ver > 0) {
            sentByAlert = input.readBoolean();
        }
    }

    @Override
    public String toString() {
        return "DelayedMassAwardDelivery[" +
                "id=" + id +
                ", errorDesc='" + errorDesc + '\'' +
                ", accountIds='" + accountIds + '\'' +
                ", sentByAlert='" + sentByAlert + '\'' +
                ']';
    }
}
