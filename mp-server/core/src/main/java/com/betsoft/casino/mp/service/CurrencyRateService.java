package com.betsoft.casino.mp.service;

import com.abs.casino.common.currency.CurrencyRate;
import com.dgphoenix.casino.common.util.Pair;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CurrencyRateService implements ICurrencyRateService {
    private final LoadingCache<Pair<String, String>, CurrencyRate> currencyRates;

    public CurrencyRateService() {
        currencyRates = CacheBuilder.newBuilder().
                expireAfterWrite(1, TimeUnit.DAYS).
                build(new CacheLoader<Pair<String, String>, CurrencyRate>() {
                    @Override
                    public CurrencyRate load(Pair<String, String> key) {
                        if (key.getKey().equals(key.getValue())) {
                            new CurrencyRate(key.getKey(), key.getValue(), 1, System.currentTimeMillis());
                        }
                        return new CurrencyRate(key.getKey(), key.getValue(), -2, System.currentTimeMillis());
                    }
                });
    }

    @Override
    public void updateCurrencyToCache(Set<CurrencyRate> cRates) {
        for (CurrencyRate cRate : cRates) {
            updateOneCurrencyToCache(cRate);
        }
    }

    @Override
    public void updateOneCurrencyToCache(CurrencyRate cRate) {
        Pair<String, String> pair = new Pair<>(cRate.getSourceCurrency(), cRate.getDestinationCurrency());
        if (cRate.getRate() > 0)
            currencyRates.put(pair, cRate);
    }

    @Override
    //todo: port curency rate functionality from GS
    public CurrencyRate get(String sourceCode, String destCode) {
        if (sourceCode.equalsIgnoreCase(destCode)) {
            return new CurrencyRate(sourceCode, destCode, 1.0, System.currentTimeMillis());
        }
        return currencyRates.getUnchecked(new Pair<>(sourceCode, destCode));
    }
}
