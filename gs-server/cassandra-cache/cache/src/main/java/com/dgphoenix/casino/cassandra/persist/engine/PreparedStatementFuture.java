package com.abs.casino.cassandra.persist.engine;

import com.google.common.util.concurrent.ListenableFuture;

public final class PreparedStatementFuture {
    private final ListenableFuture<com.datastax.driver.core.PreparedStatement> preparedStatementFuture;

    private PreparedStatementFuture(ListenableFuture<com.datastax.driver.core.PreparedStatement> preparedStatementFuture) {
        this.preparedStatementFuture = preparedStatementFuture;
    }

    static PreparedStatementFuture wrap(ListenableFuture<com.datastax.driver.core.PreparedStatement> preparedStatementFuture) {
        return preparedStatementFuture == null ? null : new PreparedStatementFuture(preparedStatementFuture);
    }

    ListenableFuture<com.datastax.driver.core.PreparedStatement> unwrap() {
        return preparedStatementFuture;
    }
}
