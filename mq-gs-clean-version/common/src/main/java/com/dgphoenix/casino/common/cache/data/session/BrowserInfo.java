package com.dgphoenix.casino.common.cache.data.session;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class BrowserInfo implements KryoSerializable {
    private static final byte VER = 0;
    private String name;
    private String version;
    private String platform;

    public BrowserInfo() {}

    public BrowserInfo(String name, String version, String platform) {
        this.name = name;
        this.version = version;
        this.platform = platform;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getPlatform() {
        return platform;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VER);
        output.writeString(name);
        output.writeString(version);
        output.writeString(platform);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        this.name = input.readString();
        this.version = input.readString();
        this.platform = input.readString();
    }

    @Override
    public String toString() {
        return "BrowserInfo{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", platform='" + platform + '\'' +
                '}';
    }
}
