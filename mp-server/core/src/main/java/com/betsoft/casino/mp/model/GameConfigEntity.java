package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.gameconfig.IGameConfig;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

public class GameConfigEntity implements IGameConfigEntity, KryoSerializable, Serializable {
    private static final byte VERSION = 0;
    private int version = 0;
    private String uploadDate;
    private String configName;
    private IGameConfig config;

    public GameConfigEntity() {}

    public GameConfigEntity(String uploadDate, String configName, IGameConfig config) {
        this.uploadDate = uploadDate;
        this.configName = configName;
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
    public String getConfigName() {
        return configName;
    }

    @Override
    public void setConfigName(String configName) {
        this.configName = configName;
    }

    @Override
    public IGameConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(IGameConfig config) {
        this.config = config;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(version, true);
        output.writeString(uploadDate);
        output.writeString(configName);
        kryo.writeClassAndObject(output, config);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        version = input.readInt(true);
        uploadDate = input.readString();
        configName = input.readString();
        config = (IGameConfig) kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameConfigEntity{");
        sb.append("version=").append(version);
        sb.append(", uploadDate='").append(uploadDate).append('\'');
        sb.append(", configName='").append(configName).append('\'');
        sb.append(", config=").append(config);
        sb.append('}');
        return sb.toString();
    }
}

