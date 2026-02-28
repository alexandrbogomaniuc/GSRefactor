package com.abs.casino.common.cache.data.server;

import com.dgphoenix.casino.common.cache.Identifiable;
import com.abs.casino.common.cache.JsonSelfSerializable;
import com.dgphoenix.casino.common.cache.data.IDistributedConfigEntry;
import com.abs.casino.common.util.hardware.data.HardwareInfo;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Date;

public class ServerInfo implements IDistributedConfigEntry, Identifiable, KryoSerializable, JsonSelfSerializable<ServerInfo> {
    private static final byte VERSION = 1;
    public static final int ALIVE_TIMEOUT = 60000;

    private int serverId;
    private String host;
    private String label;
    private int maxLoad;
    private boolean locked;
    private long uptime;
    private long updateTime;
    private volatile long startTime;

    private transient boolean thisServer = false;
    private transient boolean online = false;
    private transient boolean isMaster = false;

    public ServerInfo() {
    }

    // should be used only as DTO for GameServerConfiguration
    public ServerInfo(int serverId) {
        this(serverId, true);
    }

    public ServerInfo(int serverId, boolean thisServer) {
        this.serverId = serverId;
        this.label = "GS_" + serverId;
        this.locked = false;
        this.thisServer = thisServer;
    }

    public ServerInfo(int serverId, String label, String host, int maxLoad, long syncTime) {
        this.serverId = serverId;
        this.label = label;
        this.host = host;
        this.maxLoad = maxLoad;
        this.uptime = syncTime;
        this.updateTime = syncTime;
        this.startTime = syncTime;
        this.locked = false;
        thisServer = true;
    }

    public void copy(ServerInfo serverInfo) {
        this.host = serverInfo.host;
        this.label = serverInfo.label;
        this.maxLoad = serverInfo.maxLoad;
        this.locked = serverInfo.locked;
        this.uptime = serverInfo.uptime;
        this.updateTime = serverInfo.updateTime;
        this.startTime = serverInfo.startTime;
    }

    @Override
    public long getId() {
        return serverId;
    }

    public int getServerId() {
        return serverId;
    }

    public long getUptime() {
        return uptime;
    }

    public String getLabel() {
        return label;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setMaxLoad(int maxLoad) {
        this.maxLoad = maxLoad;
    }

    public void startup(int maxLoad, String label, String host, long syncTime) {
        if (maxLoad != this.maxLoad) {
            this.maxLoad = maxLoad;
        }
        if (!StringUtils.isTrimmedEmpty(label) && !label.equals(this.label)) {
            this.label = label;
        }
        if (!StringUtils.isTrimmedEmpty(host) && !label.equals(this.host)) {
            this.host = host;
        }
        this.updateTime = syncTime;
        this.uptime = syncTime;
        this.startTime = syncTime;
        this.locked = false;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    public int getMaxLoad() {
        return maxLoad;
    }

    public String getHost() {
        return host;
    }

    public void setUpdateTime(long millis) {
        this.updateTime = millis;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean lock) {
        this.locked = lock;
    }

    //method for getting only flag 'online'
    public boolean isServerOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setThisServer(boolean thisServer) {
        this.thisServer = thisServer;
    }

    public boolean isThisServer() {
        return thisServer;
    }

    public void touch(long syncTime) {
        updateTime = syncTime;
    }

    public void setHardwareInfo(HardwareInfo hardwareInfo) {
        //unimplemented
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setIsMaster(boolean isMaster) {
        this.isMaster = isMaster;
    }

    @Override
    public String toString() {
        return "ServerInfo [" +
                "serverId=" + this.serverId +
                ", host=" + this.host +
                ", label=" + this.label +
                ", maxLoad=" + this.maxLoad +
                ", online=" + online +
                ", isMaster=" + isMaster + 
                ", locked=" + this.locked +
                ", updateTime=" + new Date(this.updateTime) +
                ", startTime=" + new Date(this.startTime) +
                ", uptime=" + new Date(this.uptime) +
                "]";
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(serverId, true);
        output.writeString(host);
        output.writeString(label);
        output.writeInt(maxLoad);
        output.writeBoolean(locked);
        output.writeLong(uptime);
        output.writeLong(updateTime);
        output.writeLong(startTime, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        serverId = input.readInt(true);
        host = input.readString();
        label = input.readString();
        maxLoad = input.readInt();
        locked = input.readBoolean();
        uptime = input.readLong();
        updateTime = input.readLong();
        if (ver > 0) {
            startTime = input.readLong(true);
        }
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("serverId", serverId);
        gen.writeStringField("host", host);
        gen.writeStringField("label", label);
        gen.writeNumberField("maxLoad", maxLoad);
        gen.writeBooleanField("locked", locked);
        gen.writeNumberField("uptime", uptime);
        gen.writeNumberField("updateTime", updateTime);
        gen.writeNumberField("startTime", startTime);
    }

    @Override
    public ServerInfo deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        serverId = node.get("serverId").intValue();
        host = node.get("host").textValue();
        label = node.get("label").textValue();
        maxLoad = node.get("maxLoad").intValue();
        locked = node.get("locked").booleanValue();
        uptime = node.get("uptime").longValue();
        updateTime = node.get("updateTime").longValue();
        startTime = node.get("startTime").longValue();

        return this;
    }

    @Override
    public void copy(IDistributedConfigEntry entry) {
        ServerInfo copy = (ServerInfo) entry;
        serverId = copy.serverId;
        host = copy.host;
        label = copy.label;
        maxLoad = copy.maxLoad;
        locked = copy.locked;
        uptime = copy.uptime;
        updateTime = copy.updateTime;
        startTime = copy.startTime;
    }

}
