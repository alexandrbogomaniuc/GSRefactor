package com.abs.casino.common.currency;

import com.abs.casino.common.exception.CommonException;

import java.util.Collection;

/**
 * User: flsh
 * Date: 25.04.15.
 */
public interface ICurrencyRateManager {
    String DEFAULT_CURRENCY = "EUR";

    CurrencyRate NO_VALUE = new CurrencyRate("NOP", "NOP", -1, 0);

    Collection<CurrencyRate> getCurrentRates();

    public double getRateToBaseCurrency(String sourceCurrency) throws CommonException;

    double convert(double value, String sourceCurrency, String destinationCurrency) throws CommonException;
}
