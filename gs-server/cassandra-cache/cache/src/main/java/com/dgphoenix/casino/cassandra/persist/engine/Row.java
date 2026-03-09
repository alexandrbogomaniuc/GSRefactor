package com.abs.casino.cassandra.persist.engine;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Row {
    private final com.datastax.driver.core.Row row;

    Row(com.datastax.driver.core.Row row) {
        this.row = row;
    }

    static Row wrap(com.datastax.driver.core.Row row) {
        return row == null ? null : new Row(row);
    }

    com.datastax.driver.core.Row unwrap() {
        return row;
    }

    public String getString(String name) {
        return row.getString(name);
    }

    public int getInt(String name) {
        return row.getInt(name);
    }

    public long getLong(String name) {
        return row.getLong(name);
    }

    public double getDouble(String name) {
        return row.getDouble(name);
    }

    public boolean getBool(String name) {
        return row.getBool(name);
    }

    public ByteBuffer getBytes(String name) {
        return row.getBytes(name);
    }

    public UUID getUUID(String name) {
        return row.getUUID(name);
    }

    public boolean isNull(String name) {
        return row.isNull(name);
    }

    public <T> List<T> getList(String name, Class<T> elementClass) {
        return row.getList(name, elementClass);
    }

    public <T> Set<T> getSet(String name, Class<T> elementClass) {
        return row.getSet(name, elementClass);
    }

    public <K, V> Map<K, V> getMap(String name, Class<K> keyClass, Class<V> valueClass) {
        return row.getMap(name, keyClass, valueClass);
    }
}
