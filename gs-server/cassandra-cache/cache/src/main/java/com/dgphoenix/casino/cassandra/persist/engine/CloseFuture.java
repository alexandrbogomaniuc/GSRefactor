package com.abs.casino.cassandra.persist.engine;

public final class CloseFuture {
    private final com.datastax.driver.core.CloseFuture closeFuture;

    private CloseFuture(com.datastax.driver.core.CloseFuture closeFuture) {
        this.closeFuture = closeFuture;
    }

    static CloseFuture wrap(com.datastax.driver.core.CloseFuture closeFuture) {
        return closeFuture == null ? null : new CloseFuture(closeFuture);
    }

    com.datastax.driver.core.CloseFuture unwrap() {
        return closeFuture;
    }
}
