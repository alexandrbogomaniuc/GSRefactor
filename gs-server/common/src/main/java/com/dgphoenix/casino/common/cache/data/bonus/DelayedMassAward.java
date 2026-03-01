package com.abs.casino.common.cache.data.bonus;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.dgphoenix.casino.common.cache.Identifiable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.List;

/**
 * User: flsh
 * Date: 13.10.12
 */
public class DelayedMassAward implements Identifiable, IDistributedCacheEntry, KryoSerializable {
    private static final byte VERSION = 0;

    private long id;
    private long bankId;
    private List<String> extAccounts = new ArrayList<>();

    public DelayedMassAward() {}

    public DelayedMassAward(long id, long bankId) {
        this.id = id;
        this.bankId = bankId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public List<String> getExtAccounts() {
        return extAccounts;
    }

    public void setExtAccounts(List<String> extAccounts) {
        this.extAccounts = extAccounts;
    }

    public void addExtAccount(String extAccount) {
        this.extAccounts.add(extAccount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DelayedMassAward that = (DelayedMassAward) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "DelayedMassAward{" +
                "id=" + id +
                ", bankId=" + bankId +
                ", extAccounts=" + extAccounts +
                '}';
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeLong(bankId, true);
        kryo.writeClassAndObject(output, extAccounts);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        id = input.readLong(true);
        bankId = input.readLong(true);
        extAccounts = (List<String>) kryo.readClassAndObject(input);
    }
}
