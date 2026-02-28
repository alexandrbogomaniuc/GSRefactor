package com.abs.casino.common.util;

import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.bank.Limit;
import com.dgphoenix.casino.common.util.FastByteArrayOutputStream;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.esotericsoftware.kryo.util.UnsafeUtil;
import com.google.common.collect.ImmutableMap;
import de.javakaffee.kryoserializers.EnumMapSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableSetSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * User: Grien
 * Date: 01.11.2014 16:22
 */
public class FastKryoHelper {
    private static final Logger LOG = LogManager.getLogger(FastKryoHelper.class);
    protected static final byte[] EMPTY_ARRAY = new byte[]{0};
    protected static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.wrap(EMPTY_ARRAY);
    protected static final int OUTPUT_SIZE = 1024;

    //reference to queue need for monitoring queue size
    private static final Queue<Kryo> queue = new ConcurrentLinkedQueue<>();
    private static final KryoPool pool = new KryoPool.Builder(
            () -> {
                Kryo kryo = new Kryo();
                kryo.register(Coin.class, new Coin.CoinSerializer());
                kryo.register(Limit.class, new Limit.LimitSerializer());
                kryo.setReferences(false);
                kryo.addDefaultSerializer(ImmutableMap.class, new ImmutableMapSerializer());
                kryo.addDefaultSerializer(EnumMap.class, new EnumMapSerializer());
                kryo.addDefaultSerializer(Throwable.class, new JavaSerializer());
                UnmodifiableCollectionsSerializer.registerSerializers(kryo);
                ImmutableSetSerializer.registerSerializers(kryo);
                return kryo;
            })
            .softReferences()
            .queue(queue)
            .build();

    static {
        StatisticsManager.getInstance().registerStatisticsGetter("FastKryoHelper pool size", () -> String.valueOf(queue.size()));
    }

    private FastKryoHelper() {
    }

    public static boolean isEmpty(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return true;
        }
        int remaining = byteBuffer.remaining();
        if (remaining == 0) {
            return true;
        }
        if (remaining == 1) {
            int position = byteBuffer.position();
            byte b = byteBuffer.get();
            byteBuffer.position(position);
            return b == 0;
        }
        return false;
    }

    protected static ByteBufferOutput createOutput() {
        return createOutput(OUTPUT_SIZE);
    }

    protected static ByteBufferOutput createOutput(int size) {
        long now = System.currentTimeMillis();
        try {
            return new ByteBufferOutput(size, -1);//-1 => unlimited max size
        } finally {
            StatisticsManager.getInstance().updateRequestStatistics("FastKryoHelper createOutput",
                    System.currentTimeMillis() - now);
        }
    }

    public static <T> T deserializeFrom(ByteBuffer byteBuffer, Class<T> klazz) {
        long now = System.currentTimeMillis();
        try {
            if (byteBuffer == null || byteBuffer.remaining() <= 1) {
                return null;
            }
            Kryo kryo = null;
            try (Input input = new ByteBufferInput(byteBuffer)) {
                kryo = getKryo();
                return kryo.readObject(input, klazz);
            } finally {
                release(kryo);
            }
        } finally {
            StatisticsManager.getInstance().updateRequestStatistics("FastKryoHelper deserializeFrom",
                    System.currentTimeMillis() - now);
        }
    }

    public static ByteBuffer serializeToBytes(Object entity) {
        long now = System.currentTimeMillis();
        try {
            if (entity == null) {
                return EMPTY_BYTE_BUFFER;
            }
            Kryo kryo = null;
            ByteBuffer buffer = null;
            try (ByteBufferOutput output = createOutput()) {
                kryo = getKryo();
                kryo.writeObject(output, entity);
                buffer = output.getByteBuffer();
            } catch (Throwable t) {
                //noinspection ConstantConditions
                UnsafeUtil.releaseBuffer(buffer);
                throw t;
            } finally {
                release(kryo);
            }
            return (ByteBuffer) buffer.flip();
        } finally {
            StatisticsManager.getInstance().updateRequestStatistics("FastKryoHelper serializeToBytes",
                    System.currentTimeMillis() - now);
        }
    }

    public static <T extends KryoSerializable> List<T> deserializeToList(ByteBuffer serialized, Class<T> klazz, Serializer collectionSerializer) {
        long now = System.currentTimeMillis();
        try {
            if (isEmpty(serialized)) {
                return null;
            }
            Kryo kryo = null;
            try (Input input = new ByteBufferInput(serialized)) {
                kryo = getKryo();
                return (ArrayList<T>) collectionSerializer.read(kryo, input, ArrayList.class);
            } finally {
                release(kryo);
            }
        } finally {
            StatisticsManager.getInstance().updateRequestStatistics("FastKryoHelper deserializeToList",
                    System.currentTimeMillis() - now);
        }
    }

    public static <T extends KryoSerializable> ByteBuffer serializeToBytes(List<T> entities, Serializer collectionSerializer) {
        return serializeToBytes(entities, collectionSerializer, null);
    }

    public static <T extends KryoSerializable> ByteBuffer serializeToBytes(List<T> entities, Serializer collectionSerializer,
                                                                           Integer entrySizeInBytes) {
        long now = System.currentTimeMillis();
        try {
            if (entities == null) {
                return EMPTY_BYTE_BUFFER;
            }
            Kryo kryo = null;
            ByteBuffer buffer = null;
            int bufferSize = entrySizeInBytes == null ? OUTPUT_SIZE : (entrySizeInBytes * entities.size());
            try (ByteBufferOutput output = createOutput(bufferSize)) {
                kryo = getKryo();
                collectionSerializer.write(kryo, output, entities);
                buffer = output.getByteBuffer();
            } catch (Throwable t) {
                //noinspection ConstantConditions
                UnsafeUtil.releaseBuffer(buffer);
                throw t;
            } finally {
                release(kryo);
            }
            StatisticsManager.getInstance().updateRequestStatistics("FastKryoHelper collection length by single entry",
                    buffer.position() / entities.size());
            return (ByteBuffer) buffer.flip();
        } finally {
            long time = System.currentTimeMillis() - now;
            StatisticsManager.getInstance().updateRequestStatistics("FastKryoHelper serializeToBytes collection", time);
        }
    }

    public static <T> CollectionSerializer createCollectionSerializer(Class<T> entryClass) {
        Kryo kryo = null;
        try {
            kryo = FastKryoHelper.getKryo();
            Serializer beanSerializer = kryo.getSerializer(entryClass);
            return new CollectionSerializer(entryClass, beanSerializer, false);
        } finally {
            release(kryo);
        }
    }

    public static <T> T deserializeWithClassFrom(ByteBuffer byteBuffer) {
        long now = System.currentTimeMillis();
        try {
            if (isEmpty(byteBuffer)) {
                return null;
            }
            Kryo kryo = null;
            try (Input input = new ByteBufferInput(byteBuffer)) {
                kryo = getKryo();
                return (T) kryo.readClassAndObject(input);
            } finally {
                release(kryo);
            }
        } finally {
            StatisticsManager.getInstance().updateRequestStatistics("FastKryoHelper deserializeWithClassFrom",
                    System.currentTimeMillis() - now);
        }
    }

    public static ByteBuffer serializeWithClassToBytes(Object entity) {
        long now = System.currentTimeMillis();
        try {
            if (entity == null) {
                return EMPTY_BYTE_BUFFER;
            }
            Kryo kryo = null;
            ByteBuffer byteBuffer;
            try (ByteBufferOutput output = createOutput()) {
                kryo = getKryo();
                kryo.writeClassAndObject(output, entity);
                byteBuffer = output.getByteBuffer();
            } finally {
                release(kryo);
            }
            StatisticsManager.getInstance().updateRequestStatistics("FastKryoHelper buffer length",
                    byteBuffer.position());
            StatisticsManager.getInstance().updateRequestStatistics("FastKryoHelper " +
                    entity.getClass().getCanonicalName() + " buffer length", byteBuffer.position());
            return (ByteBuffer) byteBuffer.flip();
        } finally {
            StatisticsManager.getInstance().updateRequestStatistics("FastKryoHelper serializeWithClassToBytes",
                    System.currentTimeMillis() - now);
        }
    }

    public static Kryo getKryo() {
        return pool.borrow();
    }

    @Deprecated
    public static <T> T deserializeFrom(byte[] serialized, Class<T> klazz) {
        if (serialized == null || serialized.length == 0 || (serialized.length == 1 && serialized[0] == 0)) {
            return null;
        }
        Kryo kryo = getKryo();
        try (Input input = new Input(serialized)) {
            return kryo.readObject(input, klazz);
        } finally {
            release(kryo);
        }
    }

    public static void release(Kryo kryo) {
        if (kryo == null) {
            return;
        }
        kryo.reset();
        pool.release(kryo);
    }

    @Deprecated
    public static byte[] serializeToBytesArray(Object entity) {
        if (entity == null) {
            return EMPTY_ARRAY;
        }
        Kryo kryo = getKryo();
        byte[] byteArray = EMPTY_ARRAY;
        try (FastByteArrayOutputStream stream = new FastByteArrayOutputStream(512);
             Output output = new Output(stream)) {
            kryo.writeObject(output, entity);
            byteArray = stream.toByteArray();
        } catch (IOException e) {
            LOG.error("Unable to close output stream");
        } finally {
            release(kryo);
        }
        return byteArray;
    }
}
