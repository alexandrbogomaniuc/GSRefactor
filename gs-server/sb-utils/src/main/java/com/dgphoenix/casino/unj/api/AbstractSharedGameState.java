package com.abs.casino.unj.api;


import com.abs.casino.common.cache.JsonSelfSerializable;
import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.IOException;

public abstract class AbstractSharedGameState<T, SGS extends AbstractSharedGameState> implements 
        KryoSerializable, JsonSelfSerializable<SGS> {
    private static final Logger LOG = Logger.getLogger(AbstractSharedGameState.class);
    private static final int VERSION = 0;
    private long stateVersion = 0;

    public final void resetState(T resetAdditionalInfo) throws CommonException {
        reset(resetAdditionalInfo);
        ++stateVersion;
    }
    public final boolean mergeState(AbstractSharedGameState otherState) throws CommonException {
        if (!(otherState.getClass().equals(this.getClass()))) {
            throw new CommonException("Unexpected merge type = " + otherState.getClass() + ". This class type = " + this.getClass());
        }
        if (otherState.stateVersion - stateVersion > 1) {
            throw new CommonException("Only one reset per copy is available!");
        }

        LOG.debug("merge with state: " + otherState);
        if (otherState.stateVersion < stateVersion) {
            LOG.debug("Skip older reset version (other concurrent game state resets)");
            return false;
        }
        LOG.debug("merge state before: " + this.toString());
        if (otherState.stateVersion > stateVersion) {
            copyState(otherState);
            ++stateVersion; // one more increment to discard all other concurrent game state resets
        } else {
            merge(otherState);
        }
        LOG.debug("merge state after: " + this.toString());
        return true;
    }

    public final void copyState(AbstractSharedGameState otherState) throws CommonException {
        if (!(otherState.getClass().equals(this.getClass()))) {
            throw new CommonException("Unexpected copy type = " + otherState.getClass() + ". This class type = " + this.getClass());
        }
        stateVersion = otherState.stateVersion;
        copy(otherState);
    }


    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(VERSION, true);
        output.writeLong(stateVersion, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        int ver = input.readInt(true);
        if (ver >= 0) {
            stateVersion = input.readLong(true);
        }
    }

    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        

        gen.writeNumberField("stateVersion", stateVersion);


    }

    public SGS deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode n = p.getCodec().readTree(p);

        stateVersion = n.get("stateVersion").longValue();

        return getDeserializer();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE).
                append("stateVersion", stateVersion).
                append(stateToString()).toString();
    }

    abstract protected void reset(T resetAdditionalInfo) throws CommonException;
    abstract protected void merge(AbstractSharedGameState otherState) throws CommonException;
    abstract protected void copy (AbstractSharedGameState otherState) throws CommonException;
    abstract public AbstractSharedGameState copy() throws CommonException;
    abstract public String stateToString();
    abstract public SGS getDeserializer();
}
