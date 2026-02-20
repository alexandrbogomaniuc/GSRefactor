package com.dgphoenix.casino.promo.tournaments.messages;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class PrizePlaceInfo extends PlaceInfo {
    private static final byte VERSION = 0;

    private long prize;

    public PrizePlaceInfo() {}

    public PrizePlaceInfo(int place, String name, long score, long prize) {
        super(place, name, score);
        this.prize = prize;
    }

    public long getPrize() {
        return prize;
    }

    public void setPrize(long prize) {
        this.prize = prize;
    }

    @Override
    protected byte getVersion() {
        return VERSION;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeLong(prize, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        prize = input.readLong(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PrizePlaceInfo that = (PrizePlaceInfo) o;
        return prize == that.prize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), prize);
    }

    @Override
    public String toString() {
        return "PrizePlaceInfo{" +
                "prize=" + prize +
                ", place=" + place +
                ", name='" + name + '\'' +
                ", score=" + score +
                '}';
    }
}
