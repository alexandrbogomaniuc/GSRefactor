package com.abs.casino.common.feeds;

import com.abs.casino.common.cache.JsonSelfSerializable;
import com.abs.casino.tools.annotations.IgnoreValidation;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;

import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("unchecked")
@IgnoreValidation
public class FeedQueue<T> implements KryoSerializable, JsonSelfSerializable<FeedQueue> {

    private static final byte VERSION = 0;

    private transient EvictingQueue<T> queue;
    @Positive
    @Max(20)
    private int maxSize;
    private transient Lock lock = new ReentrantLock();

    public FeedQueue() {
        this.maxSize = 0;
        this.queue = EvictingQueue.create(0);
    }

    public FeedQueue(int maxSize) {
        this.maxSize = maxSize;
        this.queue = EvictingQueue.create(maxSize);
    }

    public void add(T entry) {
        lock.lock();
        try {
            queue.add(entry);
        } finally {
            lock.unlock();
        }
    }

    public List<T> getAll() {
        lock.lock();
        try {
            return (List<T>) Lists.newArrayList(queue.toArray());
        } finally {
            lock.unlock();
        }
    }

    public FeedQueue<T> addAll(List<T> entries) {
        lock.lock();
        try {
            queue.addAll(entries);
        } finally {
            lock.unlock();
        }
        return this;
    }

    public FeedQueue<T> resize(int size) {
        if (size != maxSize) {
            lock.lock();
            try {
                EvictingQueue newQueue = EvictingQueue.create(size);
                newQueue.addAll(getAll());
                queue = newQueue;
                maxSize = size;
            } finally {
                lock.unlock();
            }
        }
        return this;
    }

    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(maxSize, true);
        kryo.writeClassAndObject(output, getAll());
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        maxSize = input.readInt(true);
        queue = EvictingQueue.create(maxSize);
        List<T> entries = (List<T>) kryo.readClassAndObject(input);
        queue.addAll(entries);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("maxSize", maxSize);
        serializeListField(gen, "all", getAll(), new TypeReference<List<T>>() {});
    }

    @Override
    public FeedQueue deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        ObjectMapper om = (ObjectMapper) p.getCodec();

        maxSize = node.get("maxSize").asInt();
        List<T> entries = om.convertValue(node.get("all"), new TypeReference<List<T>>() {});
        queue.addAll(entries);

        return this;
    }
}
