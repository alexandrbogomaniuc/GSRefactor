package com.abs.casino.common.exception;

import com.dgphoenix.casino.common.currency.CurrencyRate;
import com.dgphoenix.casino.common.exception.CommonException;

public class InvalidCurrencyRateException extends CommonException {
    public InvalidCurrencyRateException(CurrencyRate rate) {
        super("Invalid currency rate = " + rate);
    }
}
