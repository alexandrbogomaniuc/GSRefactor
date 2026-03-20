package com.abs.casino.cassandra.persist.engine;

public class Statement {
    private final com.datastax.driver.core.Statement statement;

    private Statement(com.datastax.driver.core.Statement statement) {
        this.statement = statement;
    }

    public static Statement of(Object statement) {
        return statement == null ? null : new Statement((com.datastax.driver.core.Statement) statement);
    }

    static Statement wrap(com.datastax.driver.core.Statement statement) {
        return statement == null ? null : new Statement(statement);
    }

    com.datastax.driver.core.Statement unwrap() {
        return statement;
    }
}
