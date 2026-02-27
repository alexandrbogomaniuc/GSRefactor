package com.abs.casino.cassandra.persist;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 05.10.16
 */
public interface ISimplePersister {

    void persist(Object persistentObject);
}
