package com.betsoft.casino.mp.common;

import java.io.IOException;

import com.betsoft.casino.mp.model.IGameState;
import com.betsoft.casino.mp.model.IMap;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.RoomState;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * User: flsh
 * Date: 14.02.19.
 */
public abstract class AbstractClosedGameState<GAMEROOM extends IRoom, SEAT extends ISeat, MAP extends IMap, GS extends IGameState>
        extends AbstractGameState<GAMEROOM, SEAT, MAP, GS> {
    private static final byte VERSION = 0;

    public AbstractClosedGameState() {}

    public AbstractClosedGameState(GAMEROOM gameRoom) {
        super(gameRoom);
    }

    @Override
    public RoomState getRoomState() {
        return RoomState.CLOSED;
    }

    @Override
    public boolean isAllowedRemoving() {
        return true;
    }

    @Override
    public boolean isAllowedKick() {
        return false;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        

    }

    @Override
    public GS deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        return getDeserializer();
    }

    protected abstract GS getDeserializer();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClosedGameState [");
        sb.append(']');
        return sb.toString();
    }
}
