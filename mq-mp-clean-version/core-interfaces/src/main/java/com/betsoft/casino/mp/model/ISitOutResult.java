package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface ISitOutResult {
    boolean isSuccess();

    int getErrorCode();

    String getErrorDetails();
}
