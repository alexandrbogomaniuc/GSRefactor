package com.dgphoenix.casino.common.mp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

public class LeaderboardAward implements KryoSerializable, Serializable {
    private static final byte VERSION = 0;

    private int place;
    private LeaderboardAwardType type;
    private String value;

    public LeaderboardAward() {
    }

    public LeaderboardAward(int place, LeaderboardAwardType type, String value) {
        this.place = place;
        this.type = type;
        this.value = value;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public LeaderboardAwardType getType() {
        return type;
    }

    public void setType(LeaderboardAwardType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(place, true);
        output.writeInt(type.getId(), true);
        output.writeString(value);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        place = input.readInt(true);
        type = LeaderboardAwardType.getById(input.readInt(true));
        value = input.readString();
    }

    @Override
    public String toString() {
        return "LeaderboardAward{" +
                "place=" + place +
                ", type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
