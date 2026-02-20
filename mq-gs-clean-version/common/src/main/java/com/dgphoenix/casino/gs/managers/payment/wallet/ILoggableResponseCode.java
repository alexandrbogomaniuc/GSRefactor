package com.dgphoenix.casino.gs.managers.payment.wallet;

public interface ILoggableResponseCode {
    /** save response code */
    void logResponseHTTPCode(Integer code);

    Integer getResponseHTTPCode();
}
