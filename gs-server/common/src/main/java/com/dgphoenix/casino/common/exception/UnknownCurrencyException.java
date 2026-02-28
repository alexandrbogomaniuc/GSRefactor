package com.abs.casino.common.exception;

import com.dgphoenix.casino.common.exception.CommonException;

public class UnknownCurrencyException extends CommonException {
    public UnknownCurrencyException(String currencyCode) {
        super("Unknown currency = " + currencyCode);
    }
}
