package com.abs.casino.cassandra.persist.engine;

public final class PreparedStatement {
    private final com.datastax.driver.core.PreparedStatement preparedStatement;

    private PreparedStatement(com.datastax.driver.core.PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    static PreparedStatement wrap(com.datastax.driver.core.PreparedStatement preparedStatement) {
        return preparedStatement == null ? null : new PreparedStatement(preparedStatement);
    }

    com.datastax.driver.core.PreparedStatement unwrap() {
        return preparedStatement;
    }
}
