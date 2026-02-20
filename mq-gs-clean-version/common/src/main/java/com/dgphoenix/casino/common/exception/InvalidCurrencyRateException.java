package com.dgphoenix.casino.common.exception;

import com.dgphoenix.casino.common.currency.CurrencyRate;

public class InvalidCurrencyRateException extends CommonException {
    public InvalidCurrencyRateException(CurrencyRate rate) {
        super("Invalid currency rate = " + rate);
    }
}
