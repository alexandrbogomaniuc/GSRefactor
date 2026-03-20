package com.abs.casino.cassandra.persist.engine;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * User: Grien
 * Date: 24.12.2014 17:16
 */
public class FakeNotAppliedResultSet extends ResultSet {
    private com.datastax.driver.core.exceptions.DriverException exception;

    public FakeNotAppliedResultSet(com.datastax.driver.core.exceptions.DriverException exception) {
        super();
        this.exception = exception;
    }

    public com.datastax.driver.core.exceptions.DriverException getException() {
        return exception;
    }

    @Override
    public ColumnDefinitions getColumnDefinitions() {
        return null;
    }

    @Override
    public boolean isExhausted() {
        return false;
    }

    @Override
    public Row one() {
        return null;
    }

    @Override
    public List<Row> all() {
        return null;
    }

    @Override
    public Iterator<Row> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public int getAvailableWithoutFetching() {
        return 0;
    }

    @Override
    public boolean wasApplied() {
        return false;
    }

    @Override
    public String toString() {
        return "FakeNotAppliedResultSet{" +
                "exception=" + exception +
                '}';
    }
}
