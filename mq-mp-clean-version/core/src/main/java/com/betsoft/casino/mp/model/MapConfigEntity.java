package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.gameconfig.IMapConfig;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

public class MapConfigEntity implements IMapConfigEntity, KryoSerializable, Serializable {
    private static final byte VERSION = 0;
    private String uploadDate;
    private IMapConfig config;

    public MapConfigEntity() {}

    public MapConfigEntity(String uploadDate, IMapConfig config) {
        this.uploadDate = uploadDate;
        this.config = config;
    }

    @Override
    public String getUploadDate() {
        return uploadDate;
    }

    @Override
    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    @Override
    public IMapConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(IMapConfig config) {
        this.config = config;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(uploadDate);
        kryo.writeClassAndObject(output, config);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        uploadDate = input.readString();
        config = (IMapConfig) kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        return "MapConfigEntity{" +
                "uploadDate='" + uploadDate + '\'' +
                ", config=" + config +
                '}';
    }
}
