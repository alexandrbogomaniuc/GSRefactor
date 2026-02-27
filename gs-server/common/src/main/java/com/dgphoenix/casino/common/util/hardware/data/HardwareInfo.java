package com.abs.casino.common.util.hardware.data;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

public class HardwareInfo implements Serializable, KryoSerializable {
    private static final byte VERSION = 0;
    private MemoryInfo memoryInfo;
    private CPUInfo cpuInfo;

    public HardwareInfo() {
    }

    public HardwareInfo(MemoryInfo memoryInfo, CPUInfo cpuInfo) {
        this.memoryInfo = memoryInfo;
        this.cpuInfo = cpuInfo;
    }

    public MemoryInfo getMemoryInfo() {
        return memoryInfo;
    }

    public void setMemoryInfo(MemoryInfo memoryInfo) {
        this.memoryInfo = memoryInfo;
    }

    public CPUInfo getCpuInfo() {
        return cpuInfo;
    }

    public void setCpuInfo(CPUInfo cpuInfo) {
        this.cpuInfo = cpuInfo;
    }

    public String toString() {
        return "HardwareInfo [" +
                "" + this.memoryInfo +
                ", " + this.cpuInfo +
                "]";
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeObjectOrNull(output, memoryInfo, MemoryInfo.class);
        kryo.writeObjectOrNull(output, cpuInfo, CPUInfo.class);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        memoryInfo = kryo.readObjectOrNull(input, MemoryInfo.class);
        cpuInfo = kryo.readObjectOrNull(input, CPUInfo.class);
    }
}
