package com.betsoft.casino.mp.data.config;

import com.abs.casino.common.util.FastKryoHelper;
import com.abs.casino.common.util.KryoHelper;
import com.hazelcast.nio.serialization.ByteArraySerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by isador
 * on 16.06.17
 */
public class GlobalKryoSerializer<T> implements ByteArraySerializer<T> {
    private static final Logger LOG = LogManager.getLogger(GlobalKryoSerializer.class);

    @Override
    public byte[] write(T object) {
        try {
            ByteBuffer byteBuffer = FastKryoHelper.serializeWithClassToBytes(object);
            byte[] byteArray = new byte[byteBuffer.remaining()];
            byteBuffer.get(byteArray);
            return byteArray;
        } catch (Exception e) {
            LOG.error("Write error", e);
        }
        return null;
    }

    @Override
    public T read(byte[] byteArray) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
            return FastKryoHelper.deserializeWithClassFrom(byteBuffer);
        } catch (Exception e) {
            LOG.error("read error", e);
        }
        return null;
    }

    @Override
    public int getTypeId() {
        return 3;
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }
}
