package com.abs.casino.common.util;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 9/23/11
 */
public class LongPair implements Serializable, Comparable<LongPair> {
    private long first;
    private long second;

    public LongPair(long first, long second) {
        this.first = first;
        this.second = second;
    }

    public long getFirst() {
        return first;
    }

    public void setFirst(long first) {
        this.first = first;
    }

    public long getSecond() {
        return second;
    }

    public void setSecond(long second) {
        this.second = second;
    }

    public synchronized void updateByCondition(long newFirst, long newSecond, LongPairUpdateCondition condition) {
        if (condition.updateAllowed(first, second, newFirst, newSecond)) {
            first = newFirst;
            second = newSecond;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LongPair longPair = (LongPair) o;

        return first == longPair.first && second == longPair.second;
    }

    @Override
    public int hashCode() {
        int result = (int) (first ^ (first >>> 32));
        result = 31 * result + (int) (second ^ (second >>> 32));
        return result;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("LongPair");
        sb.append("[first=").append(first);
        sb.append(", second=").append(second);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int compareTo(LongPair o) {
        long thisFirst = this.first;
        long anotherFirst = o.first;
        int firstComp = (thisFirst < anotherFirst ? -1 : (thisFirst == anotherFirst ? 0 : 1));
        if (firstComp != 0) {
            return firstComp;
        }
        long thisSecond = this.second;
        long anotherSecond = o.second;
        return (thisSecond < anotherSecond ? -1 : (thisSecond == anotherSecond ? 0 : 1));
    }
}
