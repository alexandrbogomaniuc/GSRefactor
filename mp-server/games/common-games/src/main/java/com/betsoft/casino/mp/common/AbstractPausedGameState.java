package com.betsoft.casino.mp.common;

import java.io.IOException;

import com.betsoft.casino.mp.model.IGameState;
import com.betsoft.casino.mp.model.IMap;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.mp.model.room.IRoom;
import com.abs.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * State without any game logic, just for pause game on some time.
 * @param <GAMEROOM> gameroom
 * @param <SEAT> seat
 * @param <MAP> game map
 */
public abstract class AbstractPausedGameState<GAMEROOM extends IRoom, SEAT extends ISeat, MAP extends IMap, GS extends IGameState>
        extends AbstractGameState<GAMEROOM, SEAT, MAP, GS> {
    private static final byte VERSION = 0;
    IGameState nextState;

    public AbstractPausedGameState() {}

    public AbstractPausedGameState(GAMEROOM gameRoom, IGameState nextState) {
        super(gameRoom);
        this.nextState = nextState;
    }

    public abstract int getPauseTime();

    @Override
    public void init() throws CommonException {
        gameRoom.lock();
        try {
            super.init();
            gameRoom.setTimerTime(getPauseTime());
            gameRoom.startTimer();
        } finally {
            gameRoom.unlock();
        }
    }

    @Override
    public void restoreGameRoom(GAMEROOM gameRoom) throws CommonException {
        super.restoreGameRoom(gameRoom);
        gameRoom.setTimerTime(getPauseTime());
        gameRoom.startTimer();
    }

    @Override
    public void onTimer(boolean needRemoveEnemies) throws CommonException {
        gameRoom.setGameState(nextState);
    }

    @Override
    public RoomState getRoomState() {
        return RoomState.PAUSE;
    }

    @Override
    public boolean isAllowedRemoving() {
        return false;
    }

    @Override
    public boolean isAllowedKick() {
        return false;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeClassAndObject(output, nextState);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        nextState = (IGameState) kryo.readClassAndObject(input);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObjectField("nextState", nextState);
    }

    @Override
    public GS deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper om = (ObjectMapper) p.getCodec();
        JsonNode node = p.getCodec().readTree(p);
        nextState = om.convertValue(node.get("nextState"), IGameState.class);
        return getDeserializer();
    }

    protected abstract GS getDeserializer();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PausedGameState [");
        sb.append("nextState=").append(nextState);
        sb.append(']');
        return sb.toString();
    }

}

