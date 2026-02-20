package com.betsoft.casino.mp.maxcrashgame.model;

import com.betsoft.casino.mp.model.ICrashRoundInfo;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.StringJoiner;

public class CrashRoundInfo implements ICrashRoundInfo, KryoSerializable {
    private static final byte VERSION = 0;

    private double mult;
    private long startTime;
    private long roundId;
    private int bets;
    private String salt;
    private String token;

    public CrashRoundInfo() {}

    public CrashRoundInfo(double mult, long startTime, long roundId, int bets, String salt, String token) {
        this.mult = mult;
        this.startTime = startTime;
        this.roundId = roundId;
        this.bets = bets;
        this.salt = salt;
        this.token = token;
    }

    public double getMult() {
        return mult;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getRoundId() {
        return roundId;
    }

    public int getBets() {
        return bets;
    }

    public String getToken() {
        return token;
    }

    public String getSalt() {
        return salt;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeDouble(mult);
        output.writeLong(startTime, true);
        output.writeLong(roundId, true);
        output.writeInt(bets, true);
        output.writeString(salt);
        output.writeString(token);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        mult = input.readDouble();
        startTime = input.readLong(true);
        roundId = input.readLong(true);
        bets = input.readInt(true);
        salt = input.readString();
        token = input.readString();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrashRoundInfo.class.getSimpleName() + "[", "]")
                .add("mult=" + mult)
                .add("startTime=" + startTime)
                .add("roundId=" + roundId)
                .add("bets=" + bets)
                .add("salt='" + salt + "'")
                .add("token='" + token + "'")
                .toString();
    }
}
