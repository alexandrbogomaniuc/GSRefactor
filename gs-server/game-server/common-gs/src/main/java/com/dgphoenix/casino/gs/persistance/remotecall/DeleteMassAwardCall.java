package com.abs.casino.gs.persistance.remotecall;

import com.abs.casino.common.cache.MassAwardCache;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.remotecall.IRemoteCall;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 18.12.13
 */
public class DeleteMassAwardCall implements IRemoteCall, KryoSerializable {
    private static final Logger LOG = LogManager.getLogger(RefreshConfigCall.class);
    private Long id;

    public DeleteMassAwardCall() {
    }

    public DeleteMassAwardCall(Long id) {
        this.id = id;
    }

    @Override
    public void call() throws CommonException {
        MassAwardCache.getInstance().remove(id);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeleteMassAwardCall [");
        sb.append("id='").append(id).append('\'');
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeLong(id, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        id = input.readLong(true);
    }
}
