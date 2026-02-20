package com.betsoft.casino.mp.maxblastchampions.model;

import java.io.IOException;

import com.betsoft.casino.mp.common.AbstractGameRoomSnapshot;
import com.betsoft.casino.mp.model.IGameState;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

public class GameRoomSnapshot extends AbstractGameRoomSnapshot<Seat, GameMap, GameRoomSnapshot> {
    public GameRoomSnapshot() {
        super();
    }

    public GameRoomSnapshot(long roomId, long roundId, Seat[] seats, GameMap map, int nextMapId, IGameState gameState) {
        super(roomId, roundId, seats, map, nextMapId, gameState);
    }

    @Override
    protected void writeMapAndState(Kryo kryo, Output output) {
        kryo.writeObjectOrNull(output, map, GameMap.class);
        output.writeInt(nextMapId, true);
        kryo.writeClassAndObject(output, gameState);
    }

    @Override
    protected void readMapAndState(byte version, Kryo kryo, Input input) {
        map = kryo.readObjectOrNull(input, GameMap.class);
        nextMapId = input.readInt(true);
        //at this moment we cannot restore GameRoom in gameState, see usage gameState.restoreGameRoom();
        Object kryoState = kryo.readClassAndObject(input);
        if (kryoState instanceof IGameState) {
            gameState = (IGameState) kryoState;
        } else {
            LOG.error("readMapAndState: kryoState={}, this snapshot={}", kryoState, toString());
        }
    }

    @Override
    protected GameRoomSnapshot getDeserializer() {
        return this;
    }

    @Override
    protected void serializeMapAndState(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObjectField("map", map);
        gen.writeNumberField("nextMapId", nextMapId);
        gen.writeObjectField("gameState", gameState);
    }

    @Override
    protected void deserializeMapAndState(JsonParser p,
                                          JsonNode node,
                                          DeserializationContext ctxt) {
        ObjectMapper om = (ObjectMapper) p.getCodec();

        map = om.convertValue(node.get("map"), GameMap.class);
        nextMapId = node.get("nextMapId").intValue();
        //at this moment we cannot restore GameRoom in gameState, see usage gameState.restoreGameRoom();
        gameState = om.convertValue(node.get("gameState"), IGameState.class);
    }
}
