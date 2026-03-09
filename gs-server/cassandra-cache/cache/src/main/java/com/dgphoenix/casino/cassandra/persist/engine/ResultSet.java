package com.abs.casino.cassandra.persist.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResultSet implements Iterable<Row> {
    private final com.datastax.driver.core.ResultSet resultSet;

    ResultSet(com.datastax.driver.core.ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    static ResultSet wrap(com.datastax.driver.core.ResultSet resultSet) {
        return resultSet == null ? null : new ResultSet(resultSet);
    }

    com.datastax.driver.core.ResultSet unwrap() {
        return resultSet;
    }

    public Row one() {
        return Row.wrap(resultSet.one());
    }

    public List<Row> all() {
        List<com.datastax.driver.core.Row> rows = resultSet.all();
        List<Row> wrappedRows = new ArrayList<>(rows.size());
        for (com.datastax.driver.core.Row row : rows) {
            wrappedRows.add(Row.wrap(row));
        }
        return wrappedRows;
    }

    @Override
    public Iterator<Row> iterator() {
        return wrapRows(resultSet.iterator());
    }

    public boolean isExhausted() {
        return resultSet.isExhausted();
    }

    public boolean wasApplied() {
        return resultSet.wasApplied();
    }

    public int getAvailableWithoutFetching() {
        return resultSet.getAvailableWithoutFetching();
    }

    static Iterator<Row> wrapRows(final Iterator<com.datastax.driver.core.Row> iterator) {
        return new Iterator<Row>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Row next() {
                return Row.wrap(iterator.next());
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }
}
