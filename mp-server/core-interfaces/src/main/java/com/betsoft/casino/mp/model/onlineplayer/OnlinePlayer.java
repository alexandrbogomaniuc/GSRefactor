package com.betsoft.casino.mp.model.onlineplayer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class OnlinePlayer implements KryoSerializable {

    private static final byte VERSION = 0;
    private String nickname;
    private String externalId;
    private Status status;
    private long updateTime;

    public OnlinePlayer() {}

    public OnlinePlayer(String nickname, String externalId, Status status) {
        this(nickname, externalId, status, System.currentTimeMillis());
    }

    public OnlinePlayer(String nickname, String externalId, Status status, long updateTime) {
        this.nickname = nickname;
        this.externalId = externalId;
        this.status = status;
        this.updateTime = updateTime;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        this.updateTime = System.currentTimeMillis();
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
        this.updateTime = System.currentTimeMillis();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.updateTime = System.currentTimeMillis();
    }

    public boolean isOnline() {
        return getStatus() != null && getStatus() == Status.online;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(nickname);
        output.writeString(externalId);
        kryo.writeClassAndObject(output, status);
        output.writeLong(updateTime, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        //noinspection unchecked
        nickname = input.readString();
        externalId = input.readString();
        status = (Status) kryo.readClassAndObject(input);
        updateTime = input.readLong(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnlinePlayer that = (OnlinePlayer) o;
        return Objects.equals(nickname, that.nickname)
                && Objects.equals(externalId, that.externalId)
                && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickname, externalId, status);
    }

    @Override
    public String toString() {
        return "OnlinePlayer{" +
                "nickname='" + nickname + '\'' +
                ", externalId='" + externalId + '\'' +
                ", status=" + status +
                ", updateTime=" + updateTime +
                '}';
    }
}
