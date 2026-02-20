package com.dgphoenix.casino.common.cache.data.session;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class GameClientInfo implements KryoSerializable {
    private static final byte VERSION = 0;
    private long loadTime;
    private String downloadHost;

    public GameClientInfo(long loadTime, String downloadHost) {
        this.loadTime = loadTime;
        this.downloadHost = downloadHost;
    }

    public GameClientInfo() {

    }

    public long getLoadTime() {
        return loadTime;
    }

    public String getDownloadHost() {
        return downloadHost;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(loadTime, true);
        output.writeString(downloadHost);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        loadTime = input.readLong(true);
        downloadHost = input.readString();
    }

    @Override
    public String toString() {
        return "GameClientInfo{" +
                "clientLoadTime=" + downloadHost +
                ", clientDownloadHost='" + downloadHost + '\'' +
                '}';
    }
}
