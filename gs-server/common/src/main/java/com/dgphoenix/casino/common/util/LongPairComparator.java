package com.abs.casino.common.util;

import java.util.Comparator;

/**
 * User: flsh
 * Date: 2/8/12
 */
public class LongPairComparator implements Comparator<LongPair> {
    private final boolean asc;

    public LongPairComparator(boolean asc) {
        this.asc = asc;
    }

    @Override
    public int compare(LongPair p1, LongPair p2) {
        if (p1.getSecond() == p2.getSecond()) {
            if (asc) {
                return (p1.getFirst() < p2.getFirst() ? -1 : (p1.getFirst() == p2.getFirst() ? 0 : 1));
            } else {
                return (p2.getFirst() < p1.getFirst() ? -1 : (p2.getFirst() == p1.getFirst() ? 0 : 1));
            }
        } else {
            if (asc) {
                return p1.getSecond() < p2.getSecond() ? -1 : 1;
            } else {
                return p2.getSecond() < p1.getSecond() ? -1 : 1;
            }
        }
    }
}
