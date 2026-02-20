package com.dgphoenix.casino.common.util;

import com.google.common.base.Splitter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 25.10.17
 */
public class StreamUtils {

    private static Map<String, Splitter> DEFAULT_SPLITTERS;
    private static Function<String, Splitter> SPLITTER_PRODUCER;

    static {
        SPLITTER_PRODUCER = delimiter -> Splitter.on(delimiter).omitEmptyStrings();
        DEFAULT_SPLITTERS = Arrays.stream(new String[]{"|", " ", "~", "&", ","})
                .collect(Collectors.toMap(Function.identity(), SPLITTER_PRODUCER));
    }

    public static Stream<String> asStream(String string, Splitter splitter) {
        return StreamSupport.stream(splitter.split(string).spliterator(), false);
    }

    public static Stream<String> asStream(String string, String delimiter) {
        Splitter splitter = DEFAULT_SPLITTERS.get(delimiter);
        if (splitter == null) {
            splitter = SPLITTER_PRODUCER.apply(delimiter);
        }
        return StreamSupport.stream(splitter.split(string).spliterator(), false);
    }

    public static <T> Stream<T> asStream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static <T> Stream<T> asStream(Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }
}
