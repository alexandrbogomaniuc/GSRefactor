package com.abs.casino.common.currency;

import com.dgphoenix.casino.common.exception.CommonException;

public interface IHistoricalCurrencyRateManager {
    double convert(double value, Long date, String sourceCurrency, String destinationCurrency) throws CommonException;
}
