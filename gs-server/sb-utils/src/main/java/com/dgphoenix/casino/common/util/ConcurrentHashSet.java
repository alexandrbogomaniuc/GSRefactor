package com.abs.casino.common.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: flsh
 * Date: 2/20/12
 */
public class ConcurrentHashSet<T> extends AbstractSet<T> {

    // Sentinel value to represent presence in the set
    private static final Object PRESENT = new Object();

    private final ConcurrentMap<T, Object> map;

    public ConcurrentHashSet() {
        map = new ConcurrentHashMap<T, Object>();
    }

    public ConcurrentHashSet(Collection<? extends T> c) {
        map = new ConcurrentHashMap<T, Object>(Math.max((int) (c.size() / .75f) + 1, 16));
        addAll(c);
    }

    public ConcurrentHashSet(int initialCapacity, float loadFactor) {
        map = new ConcurrentHashMap<T, Object>(initialCapacity, loadFactor, 16);
    }

    public ConcurrentHashSet(int initialCapacity) {
        map = new ConcurrentHashMap<T, Object>(initialCapacity);
    }

    public Iterator<T> iterator() {
        return map.keySet().iterator();
    }

    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean add(T o) {
        return map.putIfAbsent(o, PRESENT) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) == PRESENT;
    }

    @Override
    public void clear() {
        map.clear();
    }
}
