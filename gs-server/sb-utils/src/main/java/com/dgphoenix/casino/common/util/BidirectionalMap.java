package com.abs.casino.common.util;

import java.util.*;

/**
 * Created by ANGeL
 * Date: Feb 6, 2008
 * Time: 12:16:36 PM
 */
public class BidirectionalMap<TK, TV> implements Map<TK, TV> {
    private Map<TK, TV> keysToValues;
    private Map<TV, TK> valuesToKeys;

    public BidirectionalMap() {
        keysToValues = new HashMap<TK,TV>();
        valuesToKeys = new HashMap<TV,TK>();
    }

    public BidirectionalMap(Map<TK,TV> keysToValues, Map<TV,TK> valuesToKeys) {
        this.keysToValues = keysToValues;
        this.valuesToKeys = valuesToKeys;
    }

    public int size() {
        return keysToValues.size();
    }

    public boolean isEmpty() {
        return keysToValues.isEmpty();
    }

    public boolean containsKey(Object key) {
        return keysToValues.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return keysToValues.containsValue(value);
    }

    public TV get(Object key) {
        return keysToValues.get(key);
    }

    public TV put(TK key, TV value) {
        valuesToKeys.put(value, key);
        return keysToValues.put(key, value);
    }

    public TV remove(Object key) {
        TV value = keysToValues.get(key);
        valuesToKeys.remove(value);
        return keysToValues.remove(key);
    }

    public void putAll(Map<? extends TK, ? extends TV> t) {
        Set<? extends Entry<? extends TK, ? extends TV>> entries = t.entrySet();
        for (Entry<? extends TK, ? extends TV> entry : entries) {
            valuesToKeys.put(entry.getValue(), entry.getKey());
        }
        keysToValues.putAll(t);
    }

    public void clear() {
        keysToValues.clear();
        valuesToKeys.clear();
    }

    public Set<TK> keySet() {
        return keysToValues.keySet();
    }

    public Collection<TV> values() {
        return keysToValues.values();
    }

    public Set<Entry<TK, TV>> entrySet() {
        return keysToValues.entrySet();
    }

    public boolean equals(Object o) {
        return keysToValues.equals(o);
    }

    public int hashCode() {
        return keysToValues.hashCode();
    }

    public TK getKeyForValue(TV o) {
        return valuesToKeys.get(o);
    }

    @Override
    public String toString() {
        return "BidirectionalMap{" +
               "keysToValues=" + keysToValues +
               '}';
    }
}
