package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IGameServerConfig;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.Objects;

/**
 * User: flsh
 * Date: 20.11.17.
 */
public class GameServerConfig implements IGameServerConfig, KryoSerializable, Serializable {
    private static final byte VERSION = 0;
    private int id;
    private String host;
    private String domain;
    private boolean online;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public void setDomain(String domain) {
        this.domain = domain;
    }


    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameServerConfig that = (GameServerConfig) o;
        return id == that.id &&
                Objects.equals(host, that.host) &&
                Objects.equals(domain, that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(id, true);
        output.writeString(host);
        output.writeString(domain);
        output.writeString("dummy");
        output.writeInt(0000, true);
        output.writeBoolean(online);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        id = input.readInt(true);
        host = input.readString();
        domain = input.readString();
        input.readString(); // dummy thrift host
        input.readInt(true); // dummy thrift port
        online = input.readBoolean();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameServerConfig [");
        sb.append("id=").append(id);
        sb.append(", host='").append(host).append('\'');
        sb.append(", domain='").append(domain).append('\'');
        sb.append(", online=").append(online);
        sb.append(']');
        return sb.toString();
    }
}
