package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.IGameRoomSnapshot;
import com.betsoft.casino.mp.model.IGameState;
import com.betsoft.casino.mp.model.IMap;
import com.betsoft.casino.mp.model.ISeat;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * User: flsh
 * Date: 09.02.19.
 */

/**
 * Abstract class for room snapshot. Room snapshot use during reboots  and when room moved to other server.
 * @param <SEAT> template for SEAT
 * @param <MAP> template for GAME MAP (action games)
 */
public abstract class AbstractGameRoomSnapshot<SEAT extends ISeat, MAP extends IMap, GRS extends IGameRoomSnapshot>
        implements IGameRoomSnapshot<SEAT, MAP, GRS> {
    private static final byte VERSION = 0;
    protected static final Logger LOG = LogManager.getLogger(AbstractGameRoomSnapshot.class);

    /**room id of room    */
    private long roomId;

    private long roundId;
    private transient SEAT[] seats;
    protected transient MAP map;
    protected int nextMapId;
    protected IGameState gameState;

    public AbstractGameRoomSnapshot() {
    }

    public AbstractGameRoomSnapshot(long roomId, long roundId, SEAT[] seats, MAP map, int nextMapId, IGameState gameState) {
        this.roomId = roomId;
        this.roundId = roundId;
        this.seats = seats;
        this.map = map;
        this.nextMapId = nextMapId;
        this.gameState = gameState;
    }

    @Override
    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    @Override
    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    @Override
    public SEAT[] getSeats() {
        return seats;
    }

    public void setSeats(SEAT[] seats) {
        this.seats = seats;
    }

    @Override
    public MAP getMap() {
        return map;
    }

    public void setMap(MAP map) {
        this.map = map;
    }

    public int getNextMapId() {
        return nextMapId;
    }

    public void setNextMapId(int nextMapId) {
        this.nextMapId = nextMapId;
    }

    public IGameState getGameState() {
        return gameState;
    }

    public void setGameState(IGameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(roomId, true);
        output.writeLong(roundId, true);
        kryo.writeClassAndObject(output, seats);
        writeMapAndState(kryo, output);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        roomId = input.readLong(true);
        roundId = input.readLong(true);
        seats = (SEAT[]) kryo.readClassAndObject(input);
        readMapAndState(version, kryo, input);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("roomId", roomId);
        gen.writeNumberField("roundId", roundId);
        serializeListField(gen, "seats", Arrays.asList(seats), new TypeReference<List<SEAT>>() {});
        serializeMapAndState(gen, serializers);
    }


    @Override
    public GRS deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        ObjectMapper om = (ObjectMapper) p.getCodec();

        roomId = node.get("roomId").longValue();
        roundId = node.get("roundId").longValue();
        List<SEAT> seatsL = om.convertValue(node.get("seats"), new TypeReference<List<SEAT>>() {});
        seats = (SEAT[]) seatsL.toArray();
        deserializeMapAndState(p, node, ctxt);

        return getDeserializer();
    }


    protected abstract GRS getDeserializer();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameRoomSnapshot [");
        sb.append("roomId=").append(roomId);
        sb.append(", roundId=").append(roundId);
        sb.append(", seats=").append(Arrays.toString(seats));
        sb.append(", map=").append(map);
        sb.append(", nextMapId=").append(nextMapId);
        sb.append(", gameState=").append(gameState);
        sb.append(']');
        return sb.toString();
    }

/*    protected void writeMap(Kryo kryo, Output output) {
        kryo.writeObject(output, map);
    }*/

    protected abstract void writeMapAndState(Kryo kryo, Output output);

    protected abstract void readMapAndState(byte version, Kryo kryo, Input input);

    protected abstract void serializeMapAndState(JsonGenerator gen, SerializerProvider serializers) throws IOException;

    protected abstract void deserializeMapAndState(JsonParser p,
                                                   JsonNode node,
                                                   DeserializationContext ctxt) throws IOException;
}
