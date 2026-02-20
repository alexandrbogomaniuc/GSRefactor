package com.dgphoenix.casino.gs.managers.payment.currency;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraCurrencyRatesByDatePersister;
import com.dgphoenix.casino.cassandra.persist.CassandraCurrencyRatesPersister;
import com.dgphoenix.casino.common.currency.CurrencyRate;
import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.currency.IHistoricalCurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.tracker.CurrencyUpdateProcessor;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 24.04.15.
 */
public class CurrencyRatesManager implements ICurrencyRateManager, IHistoricalCurrencyRateManager {
    private static final Logger LOG = LogManager.getLogger(CurrencyRatesManager.class);
    private static final double DEFAULT_MQB_CURRENCY_RATE = 0.1d;
    private static final Set<Pair<String, String>> FAKE_MQB_CURRENCIES;
    static {
        Set<Pair<String, String>> tempSet = new HashSet<>();
        tempSet.add(new Pair<>("MMC", DEFAULT_CURRENCY));
        tempSet.add(new Pair<>("MQC", DEFAULT_CURRENCY));
        FAKE_MQB_CURRENCIES = Collections.unmodifiableSet(tempSet);
    }

    //key is sourceCurrency/destinationCurrency, e.g. RUB/EUR
    private final LoadingCache<Pair<String, String>, CurrencyRate> rates;
    //key is sourceCurrency/destinationCurrency/date, e.g. RUB/EUR/100000
    private final LoadingCache<Triple<Long, String, String>, CurrencyRate> ratesByDate;
    private final CassandraCurrencyRatesPersister currencyRatesPersister;
    private final CassandraCurrencyRatesByDatePersister currencyRatesByDatePersister;
    private final CurrencyUpdateProcessor currencyUpdateProcessor;

    public CurrencyRatesManager(CassandraPersistenceManager cassandraPersistenceManager, CurrencyUpdateProcessor currencyUpdateProcessor) {
        currencyRatesPersister = cassandraPersistenceManager.getPersister(CassandraCurrencyRatesPersister.class);
        currencyRatesByDatePersister = cassandraPersistenceManager.getPersister(CassandraCurrencyRatesByDatePersister.class);
        this.currencyUpdateProcessor = currencyUpdateProcessor;
        rates = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(new CacheLoader<Pair<String, String>, CurrencyRate>() {
                    @Override
                    public CurrencyRate load(Pair<String, String> key) {
                        if (isFakeMQBCurrency(key)) {
                            return getFakeMQBCurrency(key);
                        }
                        if (key.getKey().equals(key.getValue())) {
                            return new CurrencyRate(key.getKey(), key.getValue(), 1.0, System.currentTimeMillis());
                        }
                        CurrencyRate rate = currencyRatesPersister.getCurrencyRate(key.getKey(), key.getValue());
                        return rate == null ? NO_VALUE : rate;
                    }
                });

        ratesByDate = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(new CacheLoader<Triple<Long, String, String>, CurrencyRate>() {
                    @Override
                    public CurrencyRate load(Triple<Long, String, String> key) {
                        if (isFakeMQBCurrency(new Pair<>(key.getMiddle(), key.getRight()))) {
                            return new CurrencyRate(key.getMiddle(), key.getRight(), DEFAULT_MQB_CURRENCY_RATE, key.getLeft());
                        }
                        if (key.getMiddle().equals(key.getRight())) {
                            return new CurrencyRate(key.getMiddle(), key.getRight(), 1.0, key.getLeft());
                        }
                        CurrencyRate rate = currencyRatesByDatePersister.getCurrencyRate(key.getLeft(), key.getMiddle(), key.getRight());
                        return rate == null ? NO_VALUE : rate;
                    }
                });

        currencyRatesPersister.registerListener(ratePair -> {
            try {
                rates.invalidate(ratePair);
            } catch (Exception e) {
                LOG.error("Cannot invalidate cache for pair: {}", ratePair, e);
            }
        });
    }

    private boolean isFakeMQBCurrency(Pair<String, String> pair) {
        return FAKE_MQB_CURRENCIES.contains(pair);
    }

    private CurrencyRate getFakeMQBCurrency(Pair<String, String> pair) {
        return new CurrencyRate(pair.getKey(), pair.getValue(), DEFAULT_MQB_CURRENCY_RATE, System.currentTimeMillis());
    }

    /**
     * @deprecated Should be used only for backward compatibility.
     */
    public static CurrencyRatesManager getInstance() {
        return ApplicationContextHelper.getApplicationContext()
                .getBean("currencyRatesManager", CurrencyRatesManager.class);
    }

    @Override
    public Collection<CurrencyRate> getCurrentRates() {
        return rates.asMap().values();
    }

    public CurrencyRate get(Pair<String, String> key) {
        return get(key, null);
    }

    public CurrencyRate get(Pair<String, String> key, Long date) {
        CurrencyRate rate;
        if (date == null) {
            rate = rates.getUnchecked(key);
        } else {
            rate = ratesByDate.getUnchecked(Triple.of(currencyRatesByDatePersister.normalizeDate(date), key.getKey(), key.getValue()));
        }
        return rate == NO_VALUE ? null : rate;
    }

    @Override
    public double getRateToBaseCurrency(String sourceCurrency) throws CommonException {
        try {
            CurrencyRate rate = rates.get(new Pair<>(sourceCurrency, DEFAULT_CURRENCY));
            if (rate == null || rate == NO_VALUE) {
                rate = new CurrencyRate(sourceCurrency, DEFAULT_CURRENCY, 1.0, 0);
                currencyRatesPersister.createOrUpdate(rate);
                currencyUpdateProcessor.updateCurrencyRateAsync(rate);
            }
            return rate.getRate();
        } catch (ExecutionException e) {
            LOG.error("Can't get rate for: sourceCurrency=" + sourceCurrency
                    + ", destinationCurrency=" + DEFAULT_CURRENCY, e);
            throw new CommonException("Can't get rate for: sourceCurrency=" + sourceCurrency
                    + ", destinationCurrency=" + DEFAULT_CURRENCY, e);
        }
    }

    public double convertAnyCurrency(double value, String sourceCurrency, String destinationCurrency) throws CommonException {
        if (sourceCurrency.equalsIgnoreCase(destinationCurrency)) {
            return value;
        }
        double sourceRate = 1.0;
        if (!sourceCurrency.equalsIgnoreCase(DEFAULT_CURRENCY)) {
            sourceRate = getValidRate(new Pair<>(sourceCurrency, DEFAULT_CURRENCY)).getRate();
        }
        BigDecimal valueInBaseCurrency = BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(sourceRate));
        CurrencyRate destRate = getValidRate(new Pair<>(destinationCurrency, DEFAULT_CURRENCY));
        if (destRate.getRate() == 1.0) {
            return valueInBaseCurrency.doubleValue();
        }
        return valueInBaseCurrency.divide(BigDecimal.valueOf(destRate.getRate()), 16, RoundingMode.DOWN).doubleValue();
    }

    @Override
    public double convert(double value, String sourceCurrency, String destinationCurrency) throws CommonException {
        return convert(value, null, sourceCurrency, destinationCurrency);
    }

    public double convert(double value, Long date, String sourceCurrency, String destinationCurrency) throws CommonException {
        if (sourceCurrency.equalsIgnoreCase(destinationCurrency)) {
            return value;
        }

        Pair<String, String> key = new Pair<>(sourceCurrency, destinationCurrency);
        try {
            CurrencyRate rate = get(key, date);
            if (rate == null) { //try use default currency for calculation
                rate = get(new Pair<>(sourceCurrency, DEFAULT_CURRENCY), date);
                if (rate == null) {
                    throw new CommonException("Cannot convert currency, rate for default currency is unknown: " + sourceCurrency);
                }
                if (rate.getRate() <= 0) {
                    throw new CommonException("Currency rate has illegal value: " + rate);
                }
                if (rate.getRate() == 1.0 && !(sourceCurrency.equalsIgnoreCase(DEFAULT_CURRENCY))) {
                    throw new CommonException("Currency rate is unknown. Rate to default currency is 1: " + rate);
                }
                BigDecimal v = BigDecimal.valueOf(value);
                BigDecimal course = BigDecimal.valueOf(rate.getRate());
                BigDecimal valueInBaseCurrency = v.multiply(course);
                CurrencyRate baseRate = get(new Pair<>(destinationCurrency, DEFAULT_CURRENCY), date);
                if (baseRate == null) {
                    throw new CommonException("CurrencyRate not found for " + destinationCurrency + " to default");
                }
                if (baseRate.getRate() <= 0) {
                    throw new CommonException("Currency rate has illegal value: " + baseRate);
                }
                if (baseRate.getRate() == 1.0 && !(destinationCurrency.equalsIgnoreCase(DEFAULT_CURRENCY))) {
                    throw new CommonException("Currency rate is unknown. Rate to default currency is 1: " + baseRate);
                }
                BigDecimal convertedRate = BigDecimal.ONE.
                        divide(BigDecimal.valueOf(baseRate.getRate()), 16, RoundingMode.DOWN);
                return valueInBaseCurrency.multiply(convertedRate).doubleValue();
            }
            if (rate.getRate() == 1.0 || rate.getRate() <= 0) {
                throw new CommonException("Currency rate is unknown: " + rate);
            }
            BigDecimal v = BigDecimal.valueOf(value);
            BigDecimal course = BigDecimal.valueOf(rate.getRate());
            return v.multiply(course).doubleValue();
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Cannot convert currency for: {}", key);
            throw new CommonException("Currency conversion error", e);
        }
    }

    private CurrencyRate getValidRate(Pair<String, String> key) throws CommonException {
        CurrencyRate rate = get(key, null);
        if (rate == null) {
            throw new CommonException("CurrencyRate is unknown for pair: " + key);
        }
        if (rate.getRate() <= 0) {
            throw new CommonException("Currency rate has illegal value: " + rate);
        }
        return rate;
    }
}
