package com.abs.casino.cassandra.persist.engine;

public class ResultSetFuture {
    private final com.datastax.driver.core.ResultSetFuture resultSetFuture;

    private ResultSetFuture(com.datastax.driver.core.ResultSetFuture resultSetFuture) {
        this.resultSetFuture = resultSetFuture;
    }

    static ResultSetFuture wrap(com.datastax.driver.core.ResultSetFuture resultSetFuture) {
        return resultSetFuture == null ? null : new ResultSetFuture(resultSetFuture);
    }

    com.datastax.driver.core.ResultSetFuture unwrap() {
        return resultSetFuture;
    }
}
