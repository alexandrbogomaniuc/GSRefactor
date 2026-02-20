package com.betsoft.casino.mp;

import java.io.IOException;

import com.betsoft.casino.mp.common.AbstractSeat;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * User: flsh
 * Date: 05.03.2022.
 */
@SuppressWarnings("rawtypes")
/**
 * Abstract class for multi node games players.
 */
public abstract class AbstractMultiNodeSeat<W extends IWeapon, P extends IPlayerRoundInfo, T extends ITreasure, RPI extends IRoomPlayerInfo, S extends ISeat>
        extends AbstractSeat<W, P, T, RPI, S> implements IMultiNodeSeat<W, P, T, RPI, S> {
    private static final byte VERSION = 1;
    protected int actualVersion = 0;
    /** roomId of room */
    protected long roomId;
    /** last activity of player */
    protected long lastActivityDate;
    /** actual serverId of player */
    protected int activeServerId = 1;

    protected AbstractMultiNodeSeat(RPI playerInfo, IGameSocketClient socketClient, double currentRate) {
        super(playerInfo, socketClient, currentRate);
        this.roomId = playerInfo.getRoomId();
        this.lastActivityDate = System.currentTimeMillis();
        if (socketClient != null) { //may be null for kryo
            this.activeServerId = socketClient.getServerId();
        }
    }

    protected AbstractMultiNodeSeat() {
    }

    @Override
    public long getRoomId() {
        return roomId;
    }

    @Override
    public long getLastActivityDate() {
        return lastActivityDate;
    }

    @Override
    public void setLastActivityDate(long lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    @Override
    public int getActualVersion() {
        return actualVersion;
    }

    @Override
    public int incrementActualVersion() {
        return ++actualVersion;
    }

    @Override
    public int getActiveServerId() {
        IGameSocketClient socketClient = getSocketClient();
        return socketClient == null ? activeServerId : socketClient.getServerId();
    }

    @Override
    public void setSocketClient(IGameSocketClient client) {
        super.setSocketClient(client);
        if (client != null) {
            this.activeServerId = client.getServerId();
        }
    }

    @Override
    protected void writeAdditionalFields(Kryo kryo, Output output) {
        super.writeAdditionalFields(kryo, output);
        output.writeByte(VERSION);
        output.writeInt(actualVersion, true);
        output.writeLong(roomId, true);
        output.writeLong(lastActivityDate, true);
        output.writeInt(activeServerId, true);
    }

    @Override
    protected void readAdditionalFields(byte version, Kryo kryo, Input input) {
        super.readAdditionalFields(version, kryo, input);
        byte ver = input.readByte();
        this.actualVersion = input.readInt(true);
        this.roomId = input.readLong(true);
        this.lastActivityDate = input.readLong(true);
        if (ver > 0) {
            activeServerId = input.readInt(true);
        }
    }

    @Override
    protected void serializeAdditionalFields(JsonGenerator gen,
                                             SerializerProvider serializers) throws IOException {
        gen.writeNumberField("actualVersion", actualVersion);
        gen.writeNumberField("roomId", roomId);
        gen.writeNumberField("lastActivityDate", lastActivityDate);
        gen.writeNumberField("activeServerId", activeServerId);
    }

    @Override
    protected void deserializeAdditionalFields(JsonParser p,
                                                        JsonNode node,
                                                        DeserializationContext ctxt) {
        this.actualVersion = node.get("actualVersion").intValue();
        this.roomId = node.get("roomId").longValue();
        this.lastActivityDate = node.get("lastActivityDate").longValue();
        this.activeServerId = node.get("activeServerId").intValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AbstractMultiNodeSeat that = (AbstractMultiNodeSeat) o;

        if (actualVersion != that.actualVersion) return false;
        if (roomId != that.roomId) return false;
        return lastActivityDate == that.lastActivityDate;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + actualVersion;
        result = 31 * result + (int) (roomId ^ (roomId >>> 32));
        result = 31 * result + (int) (lastActivityDate ^ (lastActivityDate >>> 32));
        return result;
    }
}
