package com.dgphoenix.casino.common.util;

public class Pair<K, V> {
    private K key;
    private V value;

    public Pair() {
    }

    public Pair(K key, V value) {
        super();
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (!key.equals(pair.key)) return false;
        if (value != null ? !value.equals(pair.value) : pair.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    public String toString() {
        return "Pair [" +
                "key=" + this.key +
                ", value=" + this.value +
                "]";
    }
}
