package com.abs.casino.common.cache.data.bank;

/**
 * User: Grien
 * Date: 17.08.2014 16:02
 */
public interface ILimit<T> extends Comparable<T> {
    long getId();

    int getMinValue();

    int getMaxValue();

    ILimit copy();
}
