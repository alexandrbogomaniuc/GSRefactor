package com.dgphoenix.casino.common.util.hardware.data;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

public class CPUInfo implements Serializable, KryoSerializable {
    private static final byte VERSION = 0;
    private double CPUAveragePercent;
    private int CPUsCount;

    public CPUInfo() {
    }

    public CPUInfo(double cPUAveragePercent, int cPUsCount) {
        CPUAveragePercent = cPUAveragePercent;
        CPUsCount = cPUsCount;
    }

    public double getCPUAveragePercent() {
        return CPUAveragePercent;
    }

    public void setCPUAveragePercent(double averagePercent) {
        CPUAveragePercent = averagePercent;
    }

    public int getCPUsCount() {
        return CPUsCount;
    }

    public void setCPUsCount(int usCount) {
        CPUsCount = usCount;
    }

    public String toString() {
        return "CPUInfo [" +
                "CPUAveragePercent=" + this.CPUAveragePercent +
                ", CPUsCount=" + this.CPUsCount +
                "]";
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeDouble(CPUAveragePercent);
        output.writeInt(CPUsCount);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        CPUAveragePercent = input.readDouble();
        CPUsCount = input.readInt();
    }
}
