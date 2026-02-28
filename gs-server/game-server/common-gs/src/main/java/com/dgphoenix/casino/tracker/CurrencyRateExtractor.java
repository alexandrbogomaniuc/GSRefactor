package com.abs.casino.tracker;

import com.abs.casino.common.currency.CurrencyRate;

import java.util.Collection;

public interface CurrencyRateExtractor {

    void prepare(Collection<CurrencyRate> currencyRates);

    double getRate(String sourceCurrencyCode, String targetCurrencyCode);
}
