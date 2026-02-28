package com.abs.casino.common.util.string;

import com.abs.casino.common.util.string.mappers.DoubleMapper;
import com.abs.casino.common.util.string.mappers.FromStringMapper;
import com.abs.casino.common.util.string.mappers.IntMapper;
import com.abs.casino.common.util.string.mappers.LongMapper;
import java.util.Collection;
import java.util.StringTokenizer;

public class CollectionParser {
    private static final FromStringMapper<Integer> INT_MAPPER = new IntMapper();
    private static final FromStringMapper<Double> DOUBLE_MAPPER = new DoubleMapper();
    private static final FromStringMapper<Long> LONG_MAPPER = new LongMapper();
    private String delimiter;

    public CollectionParser(String delimiter) {
        this.delimiter = delimiter;
    }

    public <T> void parse(String input, FromStringMapper<T> mapper, Collection<T> collection) {
        StringTokenizer tokenizer = new StringTokenizer(input, delimiter);
        while (tokenizer.hasMoreElements()) {
            collection.add(mapper.parse(tokenizer.nextToken()));
        }
    }

    public void parseInt(String input, Collection<Integer> collection) {
        parse(input, INT_MAPPER, collection);
    }

    public void parseDouble(String input, Collection<Double> collection) {
        parse(input, DOUBLE_MAPPER, collection);
    }

    public void parseLong(String input, Collection<Long> collection) {
        parse(input, LONG_MAPPER, collection);
    }
}
