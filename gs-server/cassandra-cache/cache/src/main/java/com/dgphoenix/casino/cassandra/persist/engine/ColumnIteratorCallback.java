package com.dgphoenix.casino.cassandra.persist.engine;

import com.datastax.driver.core.Row;

/**
 * User: flsh
 * Date: 11.10.14.
 */
public interface ColumnIteratorCallback {
    void process(Row row);
}
