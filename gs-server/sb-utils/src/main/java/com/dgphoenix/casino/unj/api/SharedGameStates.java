package com.dgphoenix.casino.unj.api;

import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.HashMap;
import java.util.Map;

public class SharedGameStates implements KryoSerializable {
    private static final int VERSION = 0;

    private Map<Long, AbstractSharedGameState> sharedGameStates = new HashMap<Long, AbstractSharedGameState>();

    //must be externally synchronized
    public void set(Long gameId, AbstractSharedGameState state) throws CommonException {
        AbstractSharedGameState exist = sharedGameStates.get(gameId);
        if (exist == null || state == null) {
            sharedGameStates.put(gameId, state);
        } else {
            exist.copyState(state);
        }
    }

    public AbstractSharedGameState get(long gameId) {
        return sharedGameStates.get(gameId);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(VERSION, true);
        kryo.writeClassAndObject(output, sharedGameStates);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        int ver = input.readInt(true);
        if (ver >= 0) {
            sharedGameStates = (Map<Long, AbstractSharedGameState>)kryo.readClassAndObject(input);
        }
    }

    @Override
    public String toString() {
        return "SharedGameStates{" +
                "sharedGameStates=" + sharedGameStates +
                '}';
    }
}
