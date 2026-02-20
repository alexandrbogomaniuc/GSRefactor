package com.dgphoenix.casino.tracker;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraCurrencyRatesConfigPersister;
import com.dgphoenix.casino.cassandra.persist.CassandraCurrencyRatesPersister;
import com.dgphoenix.casino.common.currency.CurrencyRate;
import com.dgphoenix.casino.common.util.Pair;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 12.12.17
 */
@RunWith(MockitoJUnitRunner.class)
public class CurrencyUpdateProcessorTest {

    @Mock
    private CurrencyRateExtractor rateExtractor;
    @Mock
    private CassandraPersistenceManager persistenceManager;
    @Mock
    private CassandraCurrencyRatesPersister ratesPersister;
    @Mock
    private CassandraCurrencyRatesConfigPersister ratesConfigPersister;

    private CurrencyUpdateProcessor updateProcessor;

    @Before
    public void setUp() {
        when(persistenceManager.getPersister(CassandraCurrencyRatesPersister.class)).thenReturn(ratesPersister);
        when(persistenceManager.getPersister(CassandraCurrencyRatesConfigPersister.class)).thenReturn(ratesConfigPersister);

        updateProcessor = new CurrencyUpdateProcessor(rateExtractor, persistenceManager);
    }

    @Test
    public void testAllRatesIsUpToDate() {
        configCurrencyRates(20);
        when(ratesConfigPersister.getUpdatePeriod(anyString())).thenReturn(null);

        updateProcessor.updateRates();

        verify(rateExtractor, times(0)).prepare(anyCollection());
        verify(rateExtractor, times(0)).getRate(anyString(), anyString());
    }

    @Test
    public void testAllRatesExpired() {
        configCurrencyRates(25);
        when(ratesConfigPersister.getUpdatePeriod(anyString())).thenReturn(null);

        updateProcessor.updateRates();

        verify(rateExtractor, times(1)).prepare(anyCollection());
        verify(rateExtractor, times(3)).getRate(anyString(), anyString());
        verify(ratesPersister, times(4)).createOrUpdate(any(CurrencyRate.class));
    }

    @Test
    public void testExpiredRateWithCustomUpdatePeriod() {
        configCurrencyRates(10);
        when(ratesConfigPersister.getUpdatePeriod("RUB")).thenReturn(TimeUnit.HOURS.toMillis(5));
        when(ratesConfigPersister.getUpdatePeriod("USD")).thenReturn(null);
        when(ratesConfigPersister.getUpdatePeriod("BTC")).thenReturn(null);

        updateProcessor.updateRates();

        verify(rateExtractor, times(1)).prepare(anyCollection());
        verify(rateExtractor, times(1)).getRate(anyString(), anyString());
    }

    private void configCurrencyRates(int lastUpdateHoursAgo) {
        long lastUpdateTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(lastUpdateHoursAgo);
        CurrencyRate usdRate = new CurrencyRate("USD", "EUR", 0.85, lastUpdateTime);
        CurrencyRate rubRate = new CurrencyRate("RUB", "EUR", 0.14, lastUpdateTime);
        CurrencyRate btcRate = new CurrencyRate("BTC", "EUR", 12812, lastUpdateTime);
        CurrencyRate mbcRate = new CurrencyRate("MBC", "EUR", 12.81, lastUpdateTime);
        ImmutableSet<CurrencyRate> currencyRates = ImmutableSet.of(usdRate, rubRate, btcRate, mbcRate);
        when(ratesPersister.getRates()).thenReturn(currencyRates);
        when(ratesConfigPersister.getCalculatedCurrenciesConfig()).thenReturn(ImmutableMap.of(
                "MBC", new Pair<>("BTC*0.001", "EUR")
        ));
        when(ratesPersister.getCurrencyRate("BTC", "EUR")).thenReturn(new CurrencyRate("BTC", "EUR", 20000, 0));
    }
}