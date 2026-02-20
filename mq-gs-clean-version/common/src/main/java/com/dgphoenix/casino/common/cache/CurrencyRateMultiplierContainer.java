package com.dgphoenix.casino.common.cache;

import java.util.Map;

public class CurrencyRateMultiplierContainer {
    // currency/multiplier
    private final Map<String, Integer> multipliers;

    public CurrencyRateMultiplierContainer(Map<String, Integer> multipliers) {
        this.multipliers = multipliers;
    }

    public int getMultiplier(String currency) {
        Integer multiplier = multipliers.get(currency);
        return multiplier == null ? 1 : multiplier;
    }

    @Override
    public String toString() {
        return "CurrencyRateMultipliersContainer [" + "multipliers=" + multipliers + ']';
    }
}
