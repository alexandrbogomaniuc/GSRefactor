package com.betsoft.casino.mp.piratesdmc.model;

import java.io.IOException;

import com.betsoft.casino.mp.common.AbstractMovementStrategy;
import com.betsoft.casino.mp.common.GameMapShape;
import com.betsoft.casino.mp.model.IMap;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;

public class TrajectoryMovementStrategy extends AbstractMovementStrategy<Enemy, TrajectoryMovementStrategy> {
    private static final byte VERSION = 0;

    public TrajectoryMovementStrategy() {
    }

    public TrajectoryMovementStrategy(Enemy self, IMap<Enemy, GameMapShape> map) {
        super(self, map);
    }

    @Override
    public boolean update() {
        return System.currentTimeMillis() >= self.getLeaveTime();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        //other fields from AbstractMovementStrategy (self, map) not required.
        // This prevent cyclic dependency. self and map may be restored outside
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        //other fields from AbstractMovementStrategy (self, map) not required.
        // This prevent cyclic dependency. self and map may be restored outside
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        

    }

    @Override
    public TrajectoryMovementStrategy deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        return this;
    }
}
