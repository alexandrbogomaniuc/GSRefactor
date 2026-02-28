package com.abs.casino.common.cache.data.bank;

/**
 * User: Grien
 * Date: 17.08.2014 15:58
 */
public interface ICoin<T> extends Comparable<T> {
    long getId();

    long getValue();

    ICoin copy();
}
