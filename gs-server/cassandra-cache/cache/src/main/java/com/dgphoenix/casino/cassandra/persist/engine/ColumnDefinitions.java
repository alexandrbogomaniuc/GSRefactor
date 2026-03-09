package com.abs.casino.cassandra.persist.engine;

import java.util.Iterator;

public final class ColumnDefinitions implements Iterable<ColumnDefinitions.Definition> {
    private final com.datastax.driver.core.ColumnDefinitions columnDefinitions;

    ColumnDefinitions(com.datastax.driver.core.ColumnDefinitions columnDefinitions) {
        this.columnDefinitions = columnDefinitions;
    }

    static ColumnDefinitions wrap(com.datastax.driver.core.ColumnDefinitions columnDefinitions) {
        return columnDefinitions == null ? null : new ColumnDefinitions(columnDefinitions);
    }

    com.datastax.driver.core.ColumnDefinitions unwrap() {
        return columnDefinitions;
    }

    public int size() {
        return columnDefinitions.size();
    }

    public Object getType(String name) {
        return columnDefinitions.getType(name);
    }

    @Override
    public Iterator<Definition> iterator() {
        final Iterator<com.datastax.driver.core.ColumnDefinitions.Definition> iterator = columnDefinitions.iterator();
        return new Iterator<Definition>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Definition next() {
                return Definition.wrap(iterator.next());
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    public static final class Definition {
        private final com.datastax.driver.core.ColumnDefinitions.Definition definition;

        private Definition(com.datastax.driver.core.ColumnDefinitions.Definition definition) {
            this.definition = definition;
        }

        static Definition wrap(com.datastax.driver.core.ColumnDefinitions.Definition definition) {
            return definition == null ? null : new Definition(definition);
        }

        public String getName() {
            return definition.getName();
        }

        public Object getType() {
            return definition.getType();
        }
    }
}
