/**
 * User: flsh
 * Date: Jun 28, 2010
 */
package com.dgphoenix.casino.common.cache;

import com.abs.casino.common.cache.IDistributedConfigCache;
import com.dgphoenix.casino.cassandra.persist.ICachePersister;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.exception.CurrencyNotFoundException;
import com.dgphoenix.casino.common.persist.StreamPersister;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkState;

@CacheKeyInfo(description = "currency.code")
public class CurrencyCache extends AbstractExportableCache<Currency> implements IDistributedConfigCache, ILoadingCache {
    private static final Logger LOG = LogManager.getLogger(CurrencyCache.class);
    private static final CurrencyCache instance = new CurrencyCache();

    private ICachePersister<String, Currency> persister;

    //Key - Currency.code
    private LoadingCache<String, Currency> currencies;
    private StreamPersister<String, Currency> streamPersister;

    public static CurrencyCache getInstance() {
        return instance;
    }

    private CurrencyCache() {
    }

    @SuppressWarnings("unchecked")
    public void initCache(ICachePersister<String, Currency> persister, int maxCacheSize) {
        LOG.debug("init started");
        checkState(currencies == null, "ERROR: Runtime re-init is not currently supported.");
        this.persister = persister;
        streamPersister = (StreamPersister<String, Currency>) this.persister;
        currencies = createCache(maxCacheSize);
        StatisticsManager.getInstance().registerStatisticsGetter("CurrencyCache statistics",
                () -> String.format("size=%d, stats=%s", currencies.size(), currencies.stats()));
        LOG.debug("init completed");
    }

    public void initBaseCurrencies() {
        List<Currency> currencies = new ArrayList<>();
        currencies.add(new Currency("USD", "\u0024"));
        currencies.add(new Currency("GBP", "\u20a4"));
        currencies.add(new Currency("EUR", "\u20ac"));
        currencies.add(new Currency("TRY", "TRY"));
        currencies.add(new Currency("PMC", "PMC"));
        currencies.add(new Currency("GMR", "GMR"));
        currencies.add(new Currency("GMK", "GMK"));
        currencies.add(new Currency("WDW", "WDW"));
        currencies.add(new Currency("GRU", "GRU"));
        currencies.add(new Currency("GKZ", "GKZ"));
        currencies.add(new Currency("GEL", "GEL"));
        currencies.add(new Currency("TWD", "TWD"));
        currencies.forEach(this::put);
    }

    private LoadingCache<String, Currency> createCache(int maxCacheSize) {
        return CacheBuilder.newBuilder()
                .maximumSize(maxCacheSize)
                .recordStats()
                .concurrencyLevel(8)
                .build(new CacheLoader<String, Currency>() {
                    @Override
                    public Currency load(@Nonnull String key) throws Exception {
                        Currency currency = persister.get(key);
                        if (currency != null) {
                            return currency;
                        } else {
                            throw new CommonException("Currency not found for key: " + key);
                        }
                    }
                });
    }

    private Currency putToLocalCacheIfAbsent(final String key, final Currency currency) {
        try {
            return currencies.get(key, () -> currency);
        } catch (ExecutionException e) {
            LOG.error("Can't put currency for key: " + key, e);
            return null;
        }
    }

    @Override
    public void put(Currency currency) {
        putToLocalCacheIfAbsent(currency.getCode(), currency);
    }

    @Override
    public void invalidate(String key) {
        currencies.invalidate(key);
    }

    public Currency put(String currencyCode, String currencySymbol, long bankId) throws CommonException {
        if (currencyCode.length() != Currency.CURRENCY_LENGTH) {
            throw new CurrencyNotFoundException(currencyCode);
        }

        currencyCode = currencyCode.toUpperCase();
        if (!BankInfoCache.getInstance().isExist(bankId)) {
            throw new CommonException("bank is not exist");
        }

        Currency currency = get(currencyCode);
        if (currency == null) {
            currency = new Currency(currencyCode, currencySymbol);
            put(currency);
        }

        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (!bankInfo.isCurrencyExist(currency)) {
            bankInfo.addCurrency(currency);
        }

        return currency;
    }

    public Currency get(String currencyCode) {
        try {
            return currencies.get(currencyCode);
        } catch (ExecutionException ignored) {
            return null;
        }
    }

    public boolean isExist(String currencyCode) {
        return get(currencyCode) != null;
    }

    @Override
    public void exportEntries(ObjectOutputStream outStream) throws IOException {
        streamPersister.processAll(new CacheExportProcessor<>(outStream));
    }

    @Override
    public Currency getObject(String id) {
        return currencies.getIfPresent(id);
    }

    @Override
    public Map getAllObjects() {
        return ImmutableMap.copyOf(currencies.asMap());
    }

    @Override
    public int size() {
        try {
            return Ints.checkedCast(currencies.size());
        } catch (IllegalArgumentException e) {
            LOG.debug("currencies.size()=" + currencies.size() + " can't convert to int, using -1");
            return -1;
        }
    }

    @Override
    public String getAdditionalInfo() {
        return currencies.stats().toString();
    }

    @Override
    public String printDebug() {
        return "currencies.size()=" + currencies.size();
    }

    public boolean isRequiredForImport() {
        return true;
    }

}
