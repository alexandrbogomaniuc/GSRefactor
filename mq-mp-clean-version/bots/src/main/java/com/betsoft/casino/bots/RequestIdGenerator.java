package com.betsoft.casino.bots;

import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: flsh
 * Date: 25.01.2021.
 */
public class RequestIdGenerator {
    private final AtomicInteger counter = new AtomicInteger(1);
    private static final RequestIdGenerator instance = new RequestIdGenerator();

    private RequestIdGenerator() {
    }

    public static RequestIdGenerator getInstance() {
        return instance;
    }

    public int next() {
        return counter.getAndIncrement();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RequestIdGenerator.class.getSimpleName() + "[", "]")
                .add("counter=" + counter.get())
                .toString();
    }
}
