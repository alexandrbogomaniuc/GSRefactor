package com.dgphoenix.casino.cassandra.persist;

/**
 * User: van0ss
 * Date: 28.04.2016
 */
public interface ICachePersister<K, V> {

    V get(K key);

    boolean delete(K key);

    void persist(V currency);

    void persist(K key, V currency);

}
