package com.abs.casino.cassandra.persist.engine;

/**
 * Wrapper seam for driver consistency levels.
 */
public enum ConsistencyLevel {
    ANY,
    ONE,
    TWO,
    THREE,
    QUORUM,
    ALL,
    LOCAL_QUORUM,
    EACH_QUORUM,
    SERIAL,
    LOCAL_SERIAL,
    LOCAL_ONE;

    private final com.datastax.driver.core.ConsistencyLevel value;

    ConsistencyLevel() {
        this.value = com.datastax.driver.core.ConsistencyLevel.valueOf(name());
    }

    public boolean isSerial() {
        return value.isSerial();
    }

    public static ConsistencyLevel fromName(String name) {
        return valueOf(name);
    }

    static ConsistencyLevel wrap(com.datastax.driver.core.ConsistencyLevel value) {
        return value == null ? null : ConsistencyLevel.valueOf(value.name());
    }

    public com.datastax.driver.core.ConsistencyLevel toDriver() {
        return value;
    }
}
