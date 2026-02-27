package com.abs.casino.common.promo.feed.tournament;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

public class NetworkSummaryPlayerInfo implements Serializable, KryoSerializable {
    private static final byte VERSION = 0;

    private long place;
    private String nickname;
    private long score;

    public NetworkSummaryPlayerInfo() {}

    public NetworkSummaryPlayerInfo(long place, String nickname, long score) {
        this.place = place;
        this.nickname = nickname;
        this.score = score;
    }

    public long getPlace() {
        return place;
    }

    public void setPlace(long place) {
        this.place = place;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(place);
        output.writeString(nickname);
        output.writeLong(score);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        place = input.readLong();
        nickname = input.readString();
        score = input.readLong();
    }

    @Override
    public String toString() {
        return "SummaryPlayer[" +
                "place=" + place +
                ", nickname='" + nickname + '\'' +
                ", score=" + score +
                ']';
    }
}
