package com.abs.casino.tracker;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraCurrencyRatesConfigPersister;
import com.dgphoenix.casino.cassandra.persist.CassandraCurrencyRatesPersister;
import com.abs.casino.common.currency.CurrencyRate;
import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CurrencyUpdateProcessor {
    private static final Logger LOG = LogManager.getLogger(CurrencyUpdateProcessor.class);

    public static final String USE_INTERNAL_PROPERTY = "USE_INTERNAL_CURRENCY_RATES_SOURCE";
    private static final int CHECK_FREQUENCY = 60 * 60;
    private static final long DEFAULT_UPDATE_PERIOD = TimeUnit.HOURS.toMillis(24);

    private final ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(1);
    private final ExecutorService updateRateExecutor = Executors.newSingleThreadExecutor();
    private final CurrencyRateExtractor rateExtractor;
    private final CassandraCurrencyRatesPersister ratePersister;
    private final CassandraCurrencyRatesConfigPersister rateConfigPersister;

    public CurrencyUpdateProcessor(GameServerConfiguration configuration, CassandraPersistenceManager persistenceManager) {
/*      Internal source (from CM) removed

        if (useInternalExtractor(configuration)) {
            rateExtractor = new InternalSourceCurrencyRateExtractor();
        } else {
            rateExtractor = new ExternalSourceCurrencyRateExtractor();
        }
*/
        rateExtractor = new ExternalSourceCurrencyRateExtractor();
        ratePersister = persistenceManager.getPersister(CassandraCurrencyRatesPersister.class);
        rateConfigPersister = persistenceManager.getPersister(CassandraCurrencyRatesConfigPersister.class);
    }

    protected CurrencyUpdateProcessor(CurrencyRateExtractor rateExtractor, CassandraPersistenceManager persistenceManager) {
        this.rateExtractor = rateExtractor;
        ratePersister = persistenceManager.getPersister(CassandraCurrencyRatesPersister.class);
        rateConfigPersister = persistenceManager.getPersister(CassandraCurrencyRatesConfigPersister.class);
    }

    @PostConstruct
    public void startup() {
        pool.scheduleWithFixedDelay(this::updateRates, 10, CHECK_FREQUENCY, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        LOG.info(":shutdown started:");
        ExecutorUtils.shutdownService(this.getClass().getSimpleName(), pool, 5000L);
        LOG.info("shutdown completed");
    }

    public void updateCurrencyRateAsync(CurrencyRate currencyRate) {
        updateRateExecutor.execute(() -> updateCurrencyRate(currencyRate));
    }

    public synchronized void updateRates() {
        LOG.debug("Update rates started...");
        try {
            Map<String, Pair<String, String>> calculatedCurrenciesConfig = rateConfigPersister.getCalculatedCurrenciesConfig();
            Predicate<CurrencyRate> skipCalculatedRates = skipRates(calculatedCurrenciesConfig.keySet());

            Set<CurrencyRate> expiredRates = ratePersister.getRates().stream()
                    .filter(skipCalculatedRates)
                    .filter(expired())
                    .collect(Collectors.toSet());
            LOG.debug("Found expired rates: {}", expiredRates.size());
            if (!expiredRates.isEmpty()) {
                rateExtractor.prepare(expiredRates);
                expiredRates.forEach(this::updateCurrencyRate);
                updateCalculatedCurrencies(calculatedCurrenciesConfig);
            }
        } catch (Exception e) {
            LOG.error("Unexpected error occurred during rates updating", e);
        }
        LOG.debug("Update rates finished");
    }

    private Predicate<CurrencyRate> skipRates(Set<String> ratesToSkip) {
        return currencyRate -> !ratesToSkip.contains(currencyRate.getSourceCurrency());
    }

    private void updateCurrencyRate(CurrencyRate currencyRate) {
        String currencyFrom = currencyRate.getSourceCurrency();
        String currencyTo = currencyRate.getDestinationCurrency();
        double rate = rateExtractor.getRate(currencyFrom, currencyTo);
        LOG.debug("from {} to {} old rate {} new rate {}", currencyFrom, currencyTo, currencyRate.getRate(), rate);
        currencyRate.setRate(rate);
        if (rate > 0) {
            currencyRate.setUpdateDate(System.currentTimeMillis());
        }
        persistCurrency(currencyRate);
    }

    private Predicate<CurrencyRate> expired() {
        return currencyRate -> {
            Long updatePeriod = rateConfigPersister.getUpdatePeriod(currencyRate.getSourceCurrency());
            if (updatePeriod == null) {
                updatePeriod = DEFAULT_UPDATE_PERIOD;
            }
            return System.currentTimeMillis() - currencyRate.getUpdateDate() >= updatePeriod;
        };
    }

    private void updateCalculatedCurrencies(Map<String, Pair<String, String>> config) {
        try {
            config.entrySet().stream()
                    .map(configEntry -> {
                        Pair<String, String> pair = configEntry.getValue();
                        String targetCurrency = pair.getValue();
                        String[] keyParams = pair.getKey().split("[*]");
                        String baseCurrency = keyParams[0];
                        CurrencyRate baseCurrencyRate = ratePersister.getCurrencyRate(baseCurrency, targetCurrency);
                        if (baseCurrencyRate != null && baseCurrencyRate.getRate() > 0) {
                            String calculatedCurrency = configEntry.getKey();
                            double multiplier = Double.parseDouble(keyParams[1]);
                            return new CurrencyRate(calculatedCurrency, targetCurrency, baseCurrencyRate.getRate() * multiplier, System.currentTimeMillis());
                        } else {
                            LOG.warn("Can't get rate for base currency {}:{}", baseCurrency, targetCurrency);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .forEach(this::persistCurrency);
        } catch (RuntimeException ex) {
            LOG.error("Can't parse calculated currency formula", ex);
        }
    }

    private boolean useInternalExtractor(GameServerConfiguration configuration) {
        return Optional.ofNullable(configuration.getStringPropertySilent(USE_INTERNAL_PROPERTY))
                .map("true"::equalsIgnoreCase)
                .orElse(false);
    }

    private void persistCurrency(CurrencyRate currencyRate) {
        ratePersister.createOrUpdate(currencyRate);
        LOG.info("persist from {} to {} with rate {}", currencyRate.getSourceCurrency(), currencyRate.getDestinationCurrency(), currencyRate.getRate());
    }
}