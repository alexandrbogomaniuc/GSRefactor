package com.dgphoenix.casino.common.util;

import java.util.*;

/**
 * User: flsh
 * Date: 21.03.14
 */
public class BidirectionalMultivalueMap<TK, TV> implements Map<TK, TV> {
    private Map<TK, TV> keysToValues;
    private Map<TV, Set<TK>> valuesToKeys;

    public BidirectionalMultivalueMap() {
        keysToValues = new HashMap<TK,TV>();
        valuesToKeys = new HashMap<TV,Set<TK>>();
    }

    public BidirectionalMultivalueMap(Map<TK, TV> keysToValues, Map<TV, Set<TK>> valuesToKeys) {
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
        Set<TK> keys = valuesToKeys.get(value);
        if(keys == null) {
            keys = new HashSet<TK>();
            valuesToKeys.put(value, keys);
        }
        keys.add(key);
        return keysToValues.put(key, value);
    }

    public TV remove(Object key) {
        TV value = keysToValues.get(key);
        if(value != null) {
            Set<TK> keys = valuesToKeys.get(value);
            if (keys != null) {
                keys.remove(key);
                if(keys.isEmpty()) {
                    valuesToKeys.remove(value);
                }
            }
        }
        return keysToValues.remove(key);
    }

    public void putAll(Map<? extends TK, ? extends TV> t) {
        Set<? extends Entry<? extends TK, ? extends TV>> entries = t.entrySet();
        for (Entry<? extends TK, ? extends TV> entry : entries) {
            TV value = entry.getValue();
            TK key = entry.getKey();
            Set<TK> keys = valuesToKeys.get(value);
            if(keys == null) {
                keys = new HashSet<TK>();
                valuesToKeys.put(value, keys);
            }
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

    public Set<TK> getKeysForValue(TV o) {
        return valuesToKeys.get(o);
    }

    @Override
    public String toString() {
        return "BidirectionalMap{" +
               "keysToValues=" + keysToValues +
               '}';
    }
}
