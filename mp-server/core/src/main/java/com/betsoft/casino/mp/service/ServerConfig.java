package com.betsoft.casino.mp.service;

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
public class ServerConfig implements KryoSerializable, Serializable {
    private static final byte VERSION = 1;
    private int id;

    public ServerConfig() {}

    public ServerConfig(int id) {
        this.id = id;
    }

    public int getServerId() {
        return id;
    }

    public void setServerId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerConfig that = (ServerConfig) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(id, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        id = input.readInt(true);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServerConfig [");
        sb.append("id=").append(id);
        sb.append(']');
        return sb.toString();
    }
}
