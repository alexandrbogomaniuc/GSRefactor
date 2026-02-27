package com.abs.casino.promo.tournaments.messages;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class PrizeInfo implements KryoSerializable {
    private static final byte VERSION = 0;

    private int place;
    private long prize;

    public PrizeInfo() {}

    public PrizeInfo(int place, long prize) {
        this.place = place;
        this.prize = prize;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public long getPrize() {
        return prize;
    }

    public void setPrize(long prize) {
        this.prize = prize;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(place, true);
        output.writeLong(prize, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        place = input.readInt(true);
        prize = input.readLong(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrizeInfo prizeInfo = (PrizeInfo) o;
        return place == prizeInfo.place &&
                prize == prizeInfo.prize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(place, prize);
    }

    @Override
    public String toString() {
        return "PrizeInfo{" +
                "place=" + place +
                ", prize=" + prize +
                '}';
    }
}
