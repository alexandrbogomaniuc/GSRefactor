package com.abs.casino.promo.tournaments.messages;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class PlaceInfo implements KryoSerializable {
    private static final byte VERSION = 0;

    protected int place;
    protected String name;
    protected long score;

    public PlaceInfo() {}

    public PlaceInfo(int place, String name, long score) {
        this.place = place;
        this.name = name;
        this.score = score;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    protected byte getVersion() {
        return VERSION;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(getVersion());
        output.writeInt(place, true);
        output.writeString(name);
        output.writeLong(score, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        place = input.readInt(true);
        name = input.readString();
        score = input.readLong(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaceInfo placeInfo = (PlaceInfo) o;
        return place == placeInfo.place &&
                score == placeInfo.score &&
                Objects.equals(name, placeInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(place, name, score);
    }

    @Override
    public String toString() {
        return "PlaceInfo{" +
                "place=" + place +
                ", name='" + name + '\'' +
                ", score=" + score +
                '}';
    }
}
