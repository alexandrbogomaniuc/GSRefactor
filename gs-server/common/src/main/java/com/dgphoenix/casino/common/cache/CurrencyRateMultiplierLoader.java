package com.abs.casino.common.cache;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.CurrencyRateMultiplierContainer;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.google.common.cache.CacheLoader;
import one.util.streamex.EntryStream;
import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class CurrencyRateMultiplierLoader extends CacheLoader<Long, CurrencyRateMultiplierContainer> {
    private static final Logger LOG = LogManager.getLogger(CurrencyRateMultiplierLoader.class);
    private final CurrencyRateMultiplierContainer emptyContainer;

    private final BankInfoCache bankInfoCache;

    public CurrencyRateMultiplierLoader(BankInfoCache bankInfoCache) {
        this.bankInfoCache = bankInfoCache;
        emptyContainer = new CurrencyRateMultiplierContainer(Collections.emptyMap());
    }

    @Override
    public CurrencyRateMultiplierContainer load(Long bankId) {
        BankInfo bankInfo = bankInfoCache.getBankInfo(bankId);
        if (bankInfo == null) {
            LOG.error("Cannot load CurrencyRateMultipliers for bankId={}, bankInfo not found, return EMPTY_RATE_MULTIPLIERS",
                    bankId);
            return emptyContainer;
        }
        Map<String, String> stringsMap = bankInfo.getCurrencyRateMultipliers();
        if (MapUtils.isEmpty(stringsMap)) {
            return emptyContainer;
        }
        BiFunction<String, String, Integer> parseMultiplier = (m, c) -> multiplierParser(bankId).apply(m, c);
        Map<String, Integer> multipliersMap = EntryStream.of(stringsMap)
                .mapToValue(parseMultiplier)
                .filterValues(Objects::nonNull)
                .toMap();
        return new CurrencyRateMultiplierContainer(multipliersMap);
    }

    private BiFunction<String, String, Integer> multiplierParser(Long bankId) {
        return (currency, multiplierStr) -> {
            try {
                int multiplier = Integer.parseInt(multiplierStr);
                if (multiplier < 1) {
                    LOG.error("Bad multiplier value, bankId={}, currency={}, rateMultiplier={}", bankId, currency, multiplier);
                    return null;
                }
                return multiplier;
            } catch (NumberFormatException e) {
                LOG.error("Cannot parse multiplier, bankId={}, currency={}, rateMultiplier={}", bankId, currency, multiplierStr);
                return null;
            }
        };
    }
}
