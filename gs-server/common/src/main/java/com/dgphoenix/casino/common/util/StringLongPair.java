package com.dgphoenix.casino.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: flsh
 * Date: 7/10/12
 */
public class StringLongPair implements Comparable<StringLongPair> {
    private String key;
    private long time;

    public StringLongPair(String key, long time) {
        this.key = key;
        this.time = time;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringLongPair that = (StringLongPair) o;

        if (time != that.time) return false;
        if (!key.equals(that.key)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + (int) (time ^ (time >>> 32));
        return result;
    }

    @Override
    public int compareTo(StringLongPair o) {
        long thisTime = this.time;
        long anotherTime = o.time;
        int result = (thisTime < anotherTime ? -1 : (thisTime == anotherTime ? 0 : 1));
        if (result != 0) {
            return result;
        }
        return key.compareTo(o.key);
    }


    public static void main(String[] arg) {
        List<StringLongPair> list = new ArrayList<StringLongPair>();
        list.add(new StringLongPair("bbb", 100L));
        list.add(new StringLongPair("aaa", 100L));
        list.add(new StringLongPair("zzz", 100L));
        list.add(new StringLongPair("zzz", -1L));
        list.add(new StringLongPair("axx", 1000L));
        list.add(new StringLongPair("xxx", 10L));
        list.add(new StringLongPair("axx", 10L));
        list.add(new StringLongPair("axx", -1L));
        Collections.sort(list);
        for (StringLongPair pair : list) {
            System.out.println(pair);
        }
    }

    @Override
    public String toString() {
        return "StringLongPair{" +
                "key='" + key + '\'' +
                ", time=" + time +
                '}';
    }
}
