package com.abs.casino.common.util;

public class Triple<S, T, U> {
    private final Pair<S, Pair<T, U>> triple;

    public Triple(final S first, final T second, final U third) {
        triple = new Pair<S, Pair<T, U>>(first, new Pair<T, U>(second, third));
    }

    public S first() {
        return triple.getKey();
    }

    public T second() {
        return triple.getValue().getKey();
    }

    public U third() {
        return triple.getValue().getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Triple<?, ?, ?> triple1 = (Triple<?, ?, ?>) o;
        return triple.equals(triple1.triple);
    }

    @Override
    public int hashCode() {
        return triple.hashCode();
    }

    @Override
    public String toString() {
        return "Triple[" +
                "first=" + first() +
                ",second=" + second() +
                ",third=" + third() +
                ']';
    }
}
