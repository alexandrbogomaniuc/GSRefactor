package com.dgphoenix.casino.common.exception;

public class UnknownCurrencyException extends CommonException {
    public UnknownCurrencyException(String currencyCode) {
        super("Unknown currency = " + currencyCode);
    }
}
