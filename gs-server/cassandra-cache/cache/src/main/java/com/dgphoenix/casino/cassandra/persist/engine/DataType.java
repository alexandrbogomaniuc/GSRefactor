package com.abs.casino.cassandra.persist.engine;

import java.util.Objects;

public final class DataType {
    private final com.datastax.driver.core.DataType dataType;

    private DataType(com.datastax.driver.core.DataType dataType) {
        this.dataType = Objects.requireNonNull(dataType, "dataType");
    }

    static DataType wrap(com.datastax.driver.core.DataType dataType) {
        return new DataType(dataType);
    }

    com.datastax.driver.core.DataType unwrap() {
        return dataType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataType)) {
            return false;
        }
        DataType that = (DataType) o;
        return dataType.equals(that.dataType);
    }

    @Override
    public int hashCode() {
        return dataType.hashCode();
    }

    @Override
    public String toString() {
        return dataType.toString();
    }
}
