package com.dgphoenix.casino.cassandra.persist.engine;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * User: Grien
 * Date: 24.12.2014 17:16
 */
public class FakeNotAppliedResultSet implements com.datastax.driver.core.ResultSet {
    private com.datastax.driver.core.exceptions.DriverException exception;

    public FakeNotAppliedResultSet(com.datastax.driver.core.exceptions.DriverException exception) {
        this.exception = exception;
    }

    public com.datastax.driver.core.exceptions.DriverException getException() {
        return exception;
    }

    @Override
    public com.datastax.driver.core.ColumnDefinitions getColumnDefinitions() {
        return null;
    }

    @Override
    public boolean isExhausted() {
        return false;
    }

    @Override
    public com.datastax.driver.core.Row one() {
        return null;
    }

    @Override
    public List<com.datastax.driver.core.Row> all() {
        return null;
    }

    @Override
    public Iterator<com.datastax.driver.core.Row> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public int getAvailableWithoutFetching() {
        return 0;
    }

    @Override
    public ListenableFuture<com.datastax.driver.core.ResultSet> fetchMoreResults() {
        return null;
    }

    @Override
    public boolean isFullyFetched() {
        return false;
    }


    @Override
    public com.datastax.driver.core.ExecutionInfo getExecutionInfo() {
        return null;
    }

    @Override
    public List<com.datastax.driver.core.ExecutionInfo> getAllExecutionInfo() {
        return null;
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
