package com.abs.casino.cassandra.persist.engine;

public final class CassandraDataTypes {
    private CassandraDataTypes() {
    }

    public static DataType bigint() {
        return DataType.wrap(com.datastax.driver.core.DataType.bigint());
    }

    public static DataType text() {
        return DataType.wrap(com.datastax.driver.core.DataType.text());
    }

    public static DataType blob() {
        return DataType.wrap(com.datastax.driver.core.DataType.blob());
    }

    public static DataType cint() {
        return DataType.wrap(com.datastax.driver.core.DataType.cint());
    }

    public static DataType cboolean() {
        return DataType.wrap(com.datastax.driver.core.DataType.cboolean());
    }

    public static DataType cdouble() {
        return DataType.wrap(com.datastax.driver.core.DataType.cdouble());
    }

    public static DataType counter() {
        return DataType.wrap(com.datastax.driver.core.DataType.counter());
    }

    public static DataType ascii() {
        return DataType.wrap(com.datastax.driver.core.DataType.ascii());
    }

    public static DataType varchar() {
        return DataType.wrap(com.datastax.driver.core.DataType.varchar());
    }

    public static DataType list(DataType elementType) {
        return DataType.wrap(com.datastax.driver.core.DataType.list(elementType.unwrap()));
    }

    public static DataType map(DataType keyType, DataType valueType) {
        return DataType.wrap(com.datastax.driver.core.DataType.map(keyType.unwrap(), valueType.unwrap()));
    }

    public static DataType set(DataType elementType) {
        return DataType.wrap(com.datastax.driver.core.DataType.set(elementType.unwrap()));
    }
}
