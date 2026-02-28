package com.betsoft.casino.mp.service;

import com.abs.casino.common.currency.CurrencyRate;

import java.util.Set;

/**
 * User: flsh
 * Date: 22.05.2020.
 */
public interface ICurrencyRateService {
    void updateCurrencyToCache(Set<CurrencyRate> cRates);

    void updateOneCurrencyToCache(CurrencyRate cRate);

    CurrencyRate get(String sourceCode, String destCode);
}
