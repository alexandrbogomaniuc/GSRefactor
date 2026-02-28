package com.abs.casino.common.util;

/**
 * User: flsh
 * Date: 2/11/12
 */
public interface LongPairUpdateCondition {
    boolean updateAllowed(long oldFirst, long oldSecond, long newFirst, long newSecond);
}
