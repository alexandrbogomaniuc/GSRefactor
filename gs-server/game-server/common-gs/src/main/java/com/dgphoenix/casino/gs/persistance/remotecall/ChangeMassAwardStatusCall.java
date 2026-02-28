package com.dgphoenix.casino.gs.persistance.remotecall;

import com.dgphoenix.casino.common.cache.MassAwardCache;
import com.dgphoenix.casino.common.cache.data.bonus.BaseMassAward;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.abs.casino.common.remotecall.IRemoteCall;
import com.dgphoenix.casino.common.util.string.StringUtils;
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
public class ChangeMassAwardStatusCall implements IRemoteCall, KryoSerializable {
    private static final Logger LOG = LogManager.getLogger(RefreshConfigCall.class);

    private long id;
    private BonusStatus status;

    public ChangeMassAwardStatusCall() {
    }

    public ChangeMassAwardStatusCall(long id, BonusStatus status) {
        this.id = id;
        this.status = status;
    }

    @Override
    public void call() {
        BaseMassAward massAward = MassAwardCache.getInstance().getById(id);
        if (massAward != null) {
            massAward.setStatus(status);
        } else {
            LOG.warn("Cannot change bonus status, id={}, status={}", id, status);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BonusStatus getStatus() {
        return status;
    }

    public void setStatus(BonusStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChangeMassAwardStatusCall [");
        sb.append("id='").append(id).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeLong(id, true);
        output.writeString(status == null ? null : status.name());
    }

    @Override
    public void read(Kryo kryo, Input input) {
        id = input.readLong(true);
        String s = input.readString();
        status = StringUtils.isTrimmedEmpty(s) ? null : BonusStatus.valueOf(s);
    }
}
