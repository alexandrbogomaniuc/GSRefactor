package com.dgphoenix.casino.common.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * User: Grien
 * Date: 02.08.2013 14:29
 */
public class KryoHelper {
    protected static final byte[] EMPTY_ARRAY = new byte[]{0};
    protected static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.wrap(EMPTY_ARRAY);

    protected static final ThreadLocal<Kryo> kryoInstance = new ThreadLocal<Kryo>();

    public static <T> T deserializeFrom(ByteBuffer byteBuffer, Class<T> klazz) {
        if (byteBuffer == null || byteBuffer.remaining() == 0) {
            return null;
        }
        byte[] result = new byte[byteBuffer.remaining()];
        byteBuffer.get(result);
        return deserializeFrom(result, klazz);
    }

    public static <T> T deserializeFrom(byte[] serialized, Class<T> klazz) {
        if (serialized == null || serialized.length == 0 || (serialized.length == 1 && serialized[0] == 0)) {
            return null;
        }
        Input input = new Input(serialized);
        try {
            return getKryo().readObject(input, klazz);
        } finally {
            input.close();
            getKryo().reset();
        }
    }

    public static byte[] serializeToBytes(Object entity) {
        if (entity == null) {
            return EMPTY_ARRAY;
        }
        final FastByteArrayOutputStream stream = new FastByteArrayOutputStream(512);
        Output output = new Output(stream);
        try {
            getKryo().writeObject(output, entity);
        } finally {
            output.close();
            getKryo().reset();
        }
        return stream.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static <T extends KryoSerializable> List<T> deserializeToList(byte[] serialized, Class<T> klazz,
                                                                         Serializer collectionSerializer) {
        if (serialized == null || serialized.length == 0 || (serialized.length == 1 && serialized[0] == 0)) {
            return null;
        }
        Input input = new Input(serialized);
        try {
            Kryo kryo = getKryo();
            return (ArrayList<T>) collectionSerializer.read(kryo, input, ArrayList.class);
        } finally {
            input.close();
            getKryo().reset();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends KryoSerializable> byte[] serializeToBytes(List<T> entities, Serializer collectionSerializer) {
        if (entities == null) {
            return EMPTY_ARRAY;
        }
        final FastByteArrayOutputStream stream = new FastByteArrayOutputStream(512);
        Output output = new Output(stream);
        try {
            Kryo kryo = getKryo();
            collectionSerializer.write(kryo, output, entities);
        } finally {
            output.close();
            getKryo().reset();
        }
        return stream.toByteArray();
    }

    public static <T> T deserializeWithClassFrom(ByteBuffer byteBuffer) {
        if (byteBuffer == null || byteBuffer.remaining() == 0) {
            return null;
        }
        byte[] result = new byte[byteBuffer.remaining()];
        byteBuffer.get(result);
        return deserializeWithClassFrom(result);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserializeWithClassFrom(byte[] serialized) {
        if (serialized == null || serialized.length == 0 || (serialized.length == 1 && serialized[0] == 0)) {
            return null;
        }
        Input input = new Input(serialized);
        try {
            return (T) getKryo().readClassAndObject(input);
        } finally {
            input.close();
            getKryo().reset();
        }
    }

    public static byte[] serializeWithClassToBytes(Object entity) {
        if (entity == null) {
            return EMPTY_ARRAY;
        }
        final FastByteArrayOutputStream stream = new FastByteArrayOutputStream(512);
        Output output = new Output(stream);
        try {
            getKryo().writeClassAndObject(output, entity);
        } finally {
            output.close();
            getKryo().reset();
        }
        return stream.toByteArray();
    }

    public static Kryo getKryo() {
        if (kryoInstance.get() == null) {
            final Kryo kryo = new Kryo();
            kryo.setReferences(false);
            Kryo.DefaultInstantiatorStrategy instantiatorStrategy = new Kryo.DefaultInstantiatorStrategy();
            instantiatorStrategy.setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
            kryo.setInstantiatorStrategy(instantiatorStrategy);
            kryo.addDefaultSerializer(EnumMap.class, new EnumMapSerializer());
            kryoInstance.set(kryo);

        }
        return kryoInstance.get();
    }
}