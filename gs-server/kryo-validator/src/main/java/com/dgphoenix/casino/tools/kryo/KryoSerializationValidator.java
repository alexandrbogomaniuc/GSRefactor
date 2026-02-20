package com.dgphoenix.casino.tools.kryo;

import com.dgphoenix.casino.tools.kryo.generator.RandomValueGenerator;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import org.reflections.Reflections;
import org.unitils.reflectionassert.ReflectionAssert;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Checks correctness of serialization for classes implements KryoSerializable
 *
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 13.10.2015
 */
public class KryoSerializationValidator {

    private final InstanceCreator<KryoSerializable> creator;
    private final Kryo kryo;

    public KryoSerializationValidator() {
        this.creator = new InstanceCreator<>(Collections.emptyList());
        kryo = new Kryo();
    }

    public KryoSerializationValidator(List<RandomValueGenerator> customValueGenerators) {
        this.creator = new InstanceCreator<>(customValueGenerators);
        kryo = new Kryo();
    }

    /**
     * Runs validation check for classes from specified package.
     * Creates instance for each class that implements KryoSerializable and serialized it with write method. Then deserialized it with read method
     * and performs deep comparison with original instance.
     *
     * @param packageName package name with classes fot validation
     * @return <code>true</code> if all class in package correctly serialized/deserialized and objects are equals,
     * if objects not equals throws <code>junit.framework.AssertionFailedError</code>
     */
    public boolean validate(String packageName) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends KryoSerializable>> kryoSerializable = reflections.getSubTypesOf(KryoSerializable.class);
        for (Class<? extends KryoSerializable> clazz : kryoSerializable) {
            if (creator.canCreateInstance(clazz)) {
                try {
                    KryoSerializable instance = creator.instantiateClass(clazz);
                    ByteBuffer buffer = serializeWithClassToBytes(instance);
                    KryoSerializable deserializedInstance = deserializeWithClassFrom(buffer);
                    ReflectionAssert.assertReflectionEquals(instance, deserializedInstance);
                } catch (Throwable e) {
                    System.err.println("KryoSerializationValidator:: validate error for class:" + clazz);
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        return true;
    }

    public void configure(Consumer<Kryo> consumer) {
        consumer.accept(kryo);
    }

    @SuppressWarnings("unchecked")
    public <T> T deserializeWithClassFrom(ByteBuffer byteBuffer) {
        if (isEmpty(byteBuffer)) {
            return null;
        }
        Input input = null;
        try {
            input = new ByteBufferInput(byteBuffer);
            return (T) kryo.readClassAndObject(input);
        } finally {
            close(input);
        }
    }

    @SuppressWarnings("unchecked")
    public ByteBuffer serializeWithClassToBytes(Object entity) {
        if (entity == null) {
            return ByteBuffer.wrap(new byte[]{0});
        }
        ByteBufferOutput output = null;
        ByteBuffer byteBuffer;
        try {
            output = new ByteBufferOutput(1024, -1);
            kryo.writeClassAndObject(output, entity);
            byteBuffer = output.getByteBuffer();
        } finally {
            close(output);
        }
        return (ByteBuffer) byteBuffer.flip();
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

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
                // ignore
            }
        }
    }
}
