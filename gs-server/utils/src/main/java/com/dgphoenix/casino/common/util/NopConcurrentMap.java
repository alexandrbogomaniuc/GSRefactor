package com.abs.casino.common.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * User: Grien
 * Date: 29.01.2015 17:45
 */
public class NopConcurrentMap<K, V> implements ConcurrentMap<K, V> {
    private static final NopConcurrentMap map = new NopConcurrentMap();

    public NopConcurrentMap() {
    }

    @Override
    public V putIfAbsent(K key, V value) {
        throwUnsupportedOperationException();
        return null;
    }

    @Override
    public boolean remove(Object key, Object value) {
        throwUnsupportedOperationException();
        return false;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        throwUnsupportedOperationException();
        return false;
    }

    @Override
    public V replace(K key, V value) {
        throwUnsupportedOperationException();
        return null;
    }

    @Override
    public int size() {
        throwUnsupportedOperationException();
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean containsKey(Object key) {
        throwUnsupportedOperationException();
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        throwUnsupportedOperationException();
        return false;
    }

    @Override
    public V get(Object key) {
        throwUnsupportedOperationException();
        return null;
    }

    @Override
    public V put(K key, V value) {
        throwUnsupportedOperationException();
        return null;
    }

    @Override
    public V remove(Object key) {
        throwUnsupportedOperationException();
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throwUnsupportedOperationException();
    }

    @Override
    public void clear() {
        throwUnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        throwUnsupportedOperationException();
        return null;
    }

    @Override
    public Collection<V> values() {
        throwUnsupportedOperationException();
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throwUnsupportedOperationException();
        return null;
    }

    protected void throwUnsupportedOperationException() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Implementation of ConcurrentMap with no operations. Used only for locking by this map.");
    }
}
