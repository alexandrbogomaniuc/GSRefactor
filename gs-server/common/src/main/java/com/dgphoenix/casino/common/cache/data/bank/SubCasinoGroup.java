package com.abs.casino.common.cache.data.bank;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.dgphoenix.casino.common.cache.data.IDistributedConfigEntry;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by inter on 01.06.15.
 */
public class SubCasinoGroup implements IDistributedCacheEntry, IDistributedConfigEntry, KryoSerializable {
    private static final int VERSION = 0;
    private String name = "";
    private Set<Long> subCasinoIds = new HashSet<>();

    public SubCasinoGroup() {}

    public SubCasinoGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public Set<Long> getSubCasinoList() {
        return subCasinoIds;
    }

    public synchronized void setSubCasinoList(Set<Long> subCasinoIds) {
        this.subCasinoIds = subCasinoIds;
    }

    public synchronized void addSubCasino(long subCasinoId) {
        if (subCasinoIds == null) {
            subCasinoIds = new HashSet<Long>();
        }
        if (!subCasinoIds.contains(subCasinoId)) {
            subCasinoIds.add(subCasinoId);
        }
    }

    public synchronized void removeSubCasino(long subCasinoId) {
        if (subCasinoIds != null) {
            subCasinoIds.remove(subCasinoId);
        }
    }

    @Override
    public String toString() {
        return "SubCasinoGroup[" +
                "name='" + name + '\'' +
                ", subCasinoIds=" + subCasinoIds +
                ']';
    }

    @Override
    public void copy(IDistributedConfigEntry entry) {
        SubCasinoGroup fromCopy = (SubCasinoGroup) entry;
        this.name = fromCopy.name == null ? "undefined" : fromCopy.name;
        if (subCasinoIds == null) {
            subCasinoIds = new HashSet<>();
        } else {
            subCasinoIds.clear();
        }
        if (fromCopy.subCasinoIds != null && !fromCopy.subCasinoIds.isEmpty()) {
            subCasinoIds.addAll(fromCopy.subCasinoIds);
        }
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(VERSION, true);
        output.writeString(name == null ? "" : name);
        if (subCasinoIds == null) {
            subCasinoIds = new HashSet<>();
        }
        kryo.writeClassAndObject(output, subCasinoIds);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        int ver = input.readInt(true);
        name = input.readString();
        subCasinoIds = (Set<Long>) kryo.readClassAndObject(input);
    }
}
