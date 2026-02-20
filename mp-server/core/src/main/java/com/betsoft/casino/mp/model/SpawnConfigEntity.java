package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.gameconfig.ISpawnConfig;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

public class SpawnConfigEntity implements ISpawnConfigEntity, KryoSerializable, Serializable {
    private int version = 0;
    private String uploadDate;
    private String configName;
    private ISpawnConfig config;

    public SpawnConfigEntity() {}

    public SpawnConfigEntity(String uploadDate, String configName, ISpawnConfig config) {
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
    public ISpawnConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(ISpawnConfig config) {
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
        output.writeInt(version, true);
        output.writeString(uploadDate);
        output.writeString(configName);
        kryo.writeClassAndObject(output, config);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        version = input.readInt(true);
        uploadDate = input.readString();
        configName = input.readString();
        config = (ISpawnConfig) kryo.readClassAndObject(input);
    }
}
