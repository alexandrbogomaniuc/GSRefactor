package com.abs.casino.cassandra.persist.engine;


import java.util.Objects;

/**
 * User: flsh
 * Date: 26.09.14.
 */
public class ColumnDefinition {
    private final String name;
    private final DataType type;
    private final boolean staticField;
    private final boolean indexed;
    private final boolean primaryKeyPart;

    public ColumnDefinition(String name, DataType type, boolean staticField, boolean indexed, boolean primaryKeyPart) {
        this.name = name.trim();
        this.type = type;
        this.staticField = staticField;
        this.indexed = indexed;
        this.primaryKeyPart = primaryKeyPart;
        if (staticField && (indexed || primaryKeyPart)) {
            throw new IllegalStateException("Static field cannot be indexed or primary key part");
        }
    }

    public ColumnDefinition(String name, DataType type) {
        this(name, type, false, false, false);
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    public boolean isStaticField() {
        return staticField;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public boolean isPrimaryKeyPart() {
        return primaryKeyPart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnDefinition that = (ColumnDefinition) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "ColumnDefinition{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", staticField=" + staticField +
                ", indexed=" + indexed +
                ", primaryKeyPart=" + primaryKeyPart +
                '}';
    }
}
