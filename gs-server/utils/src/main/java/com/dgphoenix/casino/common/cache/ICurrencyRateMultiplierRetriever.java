package com.abs.casino.common.cache;

public interface ICurrencyRateMultiplierRetriever {
    int getCurrencyRateMultiplier(long bankId, String currencyCode);
}
