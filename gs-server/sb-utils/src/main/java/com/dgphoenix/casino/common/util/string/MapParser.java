package com.abs.casino.common.util.string;

import com.abs.casino.common.util.string.mappers.*;
import com.google.common.base.Splitter;

import java.util.HashMap;
import java.util.Map;

public class MapParser {
    private Splitter.MapSplitter splitter;

    public MapParser(Splitter.MapSplitter splitter) {
        this.splitter = splitter;
    }

    public MapParser(String separator, String keyValueSeparator) {
        this(Splitter.on(separator).omitEmptyStrings().withKeyValueSeparator(keyValueSeparator));
    }

    public <K,V> Map<K, V> parseMap(String raw, FromStringMapper<K> keyParser, FromStringMapper<V> valueParser) {
        HashMap<K, V> result = new HashMap<K, V>();
        for (Map.Entry<String, String> entry : splitter.split(raw).entrySet()) {
            result.put(keyParser.parse(entry.getKey()), valueParser.parse(entry.getValue()));
        }
        return result;
    }

    public Map<Integer, Integer> parseIntInt(String raw) {
        return parseMap(raw, new IntMapper(), new IntMapper());
    }

    public Map<Long, Long> parseLongLong(String raw) {
        return parseMap(raw, new LongMapper(), new LongMapper());
    }

    public Map<Double, Double> parseDoubleDouble(String raw) {
        return parseMap(raw, new DoubleMapper(), new DoubleMapper());
    }

    public Map<String, Double> parseStringDouble(String raw) {
        return parseMap(raw, new StringMapper(), new DoubleMapper());
    }

    public Map<Double, String> parseDoubleString(String raw) {
        return parseMap(raw, new DoubleMapper(), new StringMapper());
    }
}
