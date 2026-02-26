package com.dgphoenix.casino.cassandra.persist.engine;


/**
 * User: flsh
 * Date: 11.10.14.
 */
public interface ColumnIteratorCallback {
    void process(com.datastax.driver.core.Row row);
}
