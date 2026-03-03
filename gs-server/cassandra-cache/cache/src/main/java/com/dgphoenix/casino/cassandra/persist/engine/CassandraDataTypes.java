package com.abs.casino.cassandra.persist.engine;

public final class CassandraDataTypes {
    private CassandraDataTypes() {
    }

    public static com.datastax.driver.core.DataType bigint() {
        return com.datastax.driver.core.DataType.bigint();
    }

    public static com.datastax.driver.core.DataType text() {
        return com.datastax.driver.core.DataType.text();
    }

    public static com.datastax.driver.core.DataType blob() {
        return com.datastax.driver.core.DataType.blob();
    }

    public static com.datastax.driver.core.DataType cint() {
        return com.datastax.driver.core.DataType.cint();
    }
}
