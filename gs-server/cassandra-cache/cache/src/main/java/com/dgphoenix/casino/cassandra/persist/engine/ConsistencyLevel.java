package com.abs.casino.cassandra.persist.engine;

/**
 * Wrapper seam for driver consistency levels.
 */
public enum ConsistencyLevel {
    ANY(com.datastax.driver.core.ConsistencyLevel.ANY),
    ONE(com.datastax.driver.core.ConsistencyLevel.ONE),
    TWO(com.datastax.driver.core.ConsistencyLevel.TWO),
    THREE(com.datastax.driver.core.ConsistencyLevel.THREE),
    QUORUM(com.datastax.driver.core.ConsistencyLevel.QUORUM),
    ALL(com.datastax.driver.core.ConsistencyLevel.ALL),
    LOCAL_QUORUM(com.datastax.driver.core.ConsistencyLevel.LOCAL_QUORUM),
    EACH_QUORUM(com.datastax.driver.core.ConsistencyLevel.EACH_QUORUM),
    SERIAL(com.datastax.driver.core.ConsistencyLevel.SERIAL),
    LOCAL_SERIAL(com.datastax.driver.core.ConsistencyLevel.LOCAL_SERIAL),
    LOCAL_ONE(com.datastax.driver.core.ConsistencyLevel.LOCAL_ONE);

    private final com.datastax.driver.core.ConsistencyLevel value;

    ConsistencyLevel(com.datastax.driver.core.ConsistencyLevel value) {
        this.value = value;
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

    com.datastax.driver.core.ConsistencyLevel unwrap() {
        return value;
    }
}
