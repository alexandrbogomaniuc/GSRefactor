package com.abs.casino.cassandra;

/**
 * Created by vladislav on 11/09/15.
 */
public interface IEntityUpdateListener<K, V> {
    void notify(K key, V newValue);
}
