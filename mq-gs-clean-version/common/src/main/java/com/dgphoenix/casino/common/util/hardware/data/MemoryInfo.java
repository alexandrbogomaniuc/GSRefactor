package com.dgphoenix.casino.common.util.hardware.data;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

public class MemoryInfo implements Serializable, KryoSerializable {
    private static final byte VERSION = 0;
    private long totalMemory;
    private long usedMemory;
    private long freeMemory;

    public MemoryInfo() {
    }

    public MemoryInfo(long totalMemory, long usedMemory, long freeMemory) {
        this.totalMemory = totalMemory;
        this.usedMemory = usedMemory;
        this.freeMemory = freeMemory;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public void setFreeMemory(long freeMemory) {
        this.freeMemory = freeMemory;
    }

    public String toString() {
        return "MemoryInfo [" +
                "totalMemory=" + this.totalMemory +
                ", usedMemory=" + this.usedMemory +
                ", freeMemory=" + this.freeMemory +
                "]";
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(totalMemory);
        output.writeLong(usedMemory);
        output.writeLong(freeMemory);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        totalMemory = input.readLong();
        usedMemory = input.readLong();
        freeMemory = input.readLong();
    }
}
