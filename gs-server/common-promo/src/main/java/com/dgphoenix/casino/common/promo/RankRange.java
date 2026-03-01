package com.abs.casino.common.promo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * User: flsh
 * Date: 11.01.17.
 * Values contract:
 * 1. start, end must be positive
 * 2. start <= end
 */
public class RankRange implements KryoSerializable {
    private static final byte VERSION = 0;

    private int start;
    private int end;

    public RankRange() {
    }

    public RankRange(int start, int end) {
        this.start = start;
        this.end = end;
        checkContract();
    }

    public boolean isInRange(int rank) {
        return rank >= start && rank <= end;
    }

    private void checkContract() {
        if(start <= 0) {
            throw new RuntimeException("start must be positive");
        }
        if(end <= 0) {
            throw new RuntimeException("end must be positive");
        }
        if(start > end) {
            throw new RuntimeException("start must be less or equals end");
        }
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(start, true);
        output.writeInt(end, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        start = input.readInt(true);
        end = input.readInt(true);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RankRange rankRange = (RankRange) o;

        if (start != rankRange.start) return false;
        return end == rankRange.end;

    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        return result;
    }

    @Override
    public String toString() {
        return "RankRange[" +
                "start=" + start +
                ", end=" + end +
                ']';
    }
}
