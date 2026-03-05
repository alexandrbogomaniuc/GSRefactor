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

    public static com.datastax.driver.core.DataType cboolean() {
        return com.datastax.driver.core.DataType.cboolean();
    }

    public static com.datastax.driver.core.DataType cdouble() {
        return com.datastax.driver.core.DataType.cdouble();
    }

    public static com.datastax.driver.core.DataType varchar() {
        return com.datastax.driver.core.DataType.varchar();
    }

    public static com.datastax.driver.core.DataType list(com.datastax.driver.core.DataType elementType) {
        return com.datastax.driver.core.DataType.list(elementType);
    }

    public static com.datastax.driver.core.DataType set(com.datastax.driver.core.DataType elementType) {
        return com.datastax.driver.core.DataType.set(elementType);
    }
}
