package com.dgphoenix.casino.gs.persistance.remotecall;

import com.abs.casino.cache.CachesHolder;
import com.dgphoenix.casino.cassandra.persist.AbstractDistributedConfigEntryPersister;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.remotecall.IRemoteCall;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 9/28/12
 */
public class RefreshConfigCall implements IRemoteCall, KryoSerializable {
    private static final Logger LOG = LogManager.getLogger(RefreshConfigCall.class);
    private String configName;
    private String id;

    public RefreshConfigCall() {
    }

    public RefreshConfigCall(String configName, String id) {
        this.configName = configName;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }


    @Override
    public void call() throws CommonException {
        CachesHolder cachesHolder = ApplicationContextHelper.getApplicationContext()
                .getBean("cachesHolder", CachesHolder.class);
        AbstractCassandraPersister persister = cachesHolder.getConfigPersistersMap().get(configName);
        if (persister == null) {
            LOG.error("Unknown config: " + configName + ", id=" + id);
        } else {
            if (AbstractDistributedConfigEntryPersister.class.isInstance(persister)) {
                ((AbstractDistributedConfigEntryPersister) persister).refresh(id);
            }
        }
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("RefreshConfigCall");
        sb.append("[configName='").append(configName).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeString(configName);
        output.writeString(id);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        configName = input.readString();
        id = input.readString();
    }
}
