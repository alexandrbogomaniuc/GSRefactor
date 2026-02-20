package com.dgphoenix.casino.common.util.string;

import java.io.Serializable;
import java.io.Writer;

/**
 * User: flsh
 * Date: 30.08.14.
 */
public class StringBuilderWriter extends Writer implements Serializable {

    private StringBuilder builder;

    public StringBuilderWriter() {
        this.builder = new StringBuilder(128);
    }

    public StringBuilderWriter(int capacity) {
        this.builder = new StringBuilder(capacity);
    }

    public StringBuilderWriter(StringBuilder builder) {
        this.builder = builder != null ? builder : new StringBuilder();
    }

    public void setBuilder(StringBuilder builder) {
        this.builder = builder;
    }

    public void clear() {
        builder.setLength(0);
    }

    @Override
    public Writer append(CharSequence value) {
        builder.append(value);
        return this;
    }

    @Override
    public Writer append(CharSequence value, int start, int end) {
        builder.append(value, start, end);
        return this;
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }


    @Override
    public void write(String value) {
        if (value != null) {
            builder.append(value);
        }
    }

    @Override
    public void write(char[] value, int offset, int length) {
        if (value != null) {
            builder.append(value, offset, length);
        }
    }

    public StringBuilder getBuilder() {
        return builder;
    }

    @Override
    public String toString() {
        return builder.toString();
    }

}
