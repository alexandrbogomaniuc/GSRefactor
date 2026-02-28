package com.abs.casino.gs.managers.payment.currency;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraCurrencyPersister;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.abs.casino.common.currency.CurrencyRate;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.exception.InvalidCurrencyRateException;
import com.abs.casino.common.exception.UnknownCurrencyException;
import com.dgphoenix.casino.common.util.Pair;
import com.abs.casino.gs.managers.payment.currency.CurrencyManager;
import com.abs.casino.gs.managers.payment.currency.CurrencyRatesManager;
import com.abs.casino.gs.persistance.remotecall.RemoteCallHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CurrencyManagerTest {
    private static final long BANK_ID = 123L;
    private static final String CURRENCY_CODE = "JPY";
    private static final String CURRENCY_SYMBOL = "¥";

    @Mock
    private CurrencyRatesManager currencyRatesManager;
    @Mock
    private CassandraCurrencyPersister currencyPersister;
    @Mock
    private RemoteCallHelper remoteCallHelper;
    @Mock
    private CurrencyCache currencyCache;
    @Mock
    private BankInfoCache bankInfoCache;
    @Mock
    private CassandraPersistenceManager cassandraPersistenceManager;
    @Mock
    private ApplicationContext applicationContext;
    private CurrencyManager currencyManager;
    private Currency expectedCurrency;

    @Before
    public void setUp() {
        when(cassandraPersistenceManager.getPersister(CassandraCurrencyPersister.class)).thenReturn(currencyPersister);
        when(applicationContext.getBean(CassandraPersistenceManager.class)).thenReturn(cassandraPersistenceManager);
        when(applicationContext.getBean(RemoteCallHelper.class)).thenReturn(remoteCallHelper);
        when(applicationContext.getBean(CurrencyRatesManager.class)).thenReturn(currencyRatesManager);
        currencyManager = new TestCurrencyManager(applicationContext, currencyCache, bankInfoCache);
        expectedCurrency = new Currency(CURRENCY_CODE, CURRENCY_SYMBOL);
    }

    @Test(expected = UnknownCurrencyException.class)
    public void testSetupUnknownCurrency() throws CommonException {
        when(currencyCache.get(anyString())).thenReturn(null);

        currencyManager.setupCurrency(CURRENCY_CODE, CURRENCY_SYMBOL, BANK_ID);
    }

    @Test(expected = CommonException.class)
    public void testSetupCurrencyToUnknownBank() throws CommonException {
        when(currencyCache.get(CURRENCY_CODE)).thenReturn(expectedCurrency);
        when(bankInfoCache.getBankInfo(anyLong())).thenReturn(null);

        currencyManager.setupCurrency(CURRENCY_CODE, CURRENCY_SYMBOL, BANK_ID);
    }

    @Test(expected = InvalidCurrencyRateException.class)
    public void testSetupCurrencyWithInvalidRate() throws CommonException {
        when(currencyCache.get(CURRENCY_CODE)).thenReturn(expectedCurrency);
        BankInfo bankInfo = mock(BankInfo.class);
        when(bankInfo.isCurrencyExist(expectedCurrency)).thenReturn(false);
        when(bankInfo.isCurrencyCodeAllowed(CURRENCY_CODE)).thenReturn(true);
        when(bankInfoCache.getBankInfo(BANK_ID)).thenReturn(bankInfo);
        CurrencyRate currencyRate = mock(CurrencyRate.class);
        when(currencyRate.getRate()).thenReturn(1.0);
        when(currencyRatesManager.get(anyObject())).thenReturn(currencyRate);

        currencyManager.setupCurrency(CURRENCY_CODE, CURRENCY_SYMBOL, BANK_ID);
    }

    @Test(expected = InvalidCurrencyRateException.class)
    public void testSetupCurrencyWithHugeRate() throws CommonException {
        when(currencyCache.get(CURRENCY_CODE)).thenReturn(expectedCurrency);
        BankInfo bankInfo = mock(BankInfo.class);
        when(bankInfo.isCurrencyExist(expectedCurrency)).thenReturn(false);
        when(bankInfo.isCurrencyCodeAllowed(CURRENCY_CODE)).thenReturn(true);
        when(bankInfoCache.getBankInfo(BANK_ID)).thenReturn(bankInfo);
        CurrencyRate currencyRate = mock(CurrencyRate.class);
        when(currencyRate.getRate()).thenReturn(50.1);
        when(currencyRatesManager.get(any(Pair.class))).thenReturn(currencyRate);

        currencyManager.setupCurrency(CURRENCY_CODE, CURRENCY_SYMBOL, BANK_ID);
    }

    @Test(expected = InvalidCurrencyRateException.class)
    public void testSetupCurrencyWithMiserableRate() throws CommonException {
        when(currencyCache.get(CURRENCY_CODE)).thenReturn(expectedCurrency);
        BankInfo bankInfo = mock(BankInfo.class);
        when(bankInfo.isCurrencyExist(expectedCurrency)).thenReturn(false);
        when(bankInfo.isCurrencyCodeAllowed(CURRENCY_CODE)).thenReturn(true);
        when(bankInfoCache.getBankInfo(BANK_ID)).thenReturn(bankInfo);
        CurrencyRate currencyRate = mock(CurrencyRate.class);
        when(currencyRate.getRate()).thenReturn(0.00089);
        when(currencyRatesManager.get(any(Pair.class))).thenReturn(currencyRate);

        currencyManager.setupCurrency(CURRENCY_CODE, CURRENCY_SYMBOL, BANK_ID);
    }

    @Test(expected = CommonException.class)
    public void testSetupNotAllowedCurrency() throws CommonException {
        when(currencyCache.get(CURRENCY_CODE)).thenReturn(expectedCurrency);
        BankInfo bankInfo = mock(BankInfo.class);
        when(bankInfo.isCurrencyCodeAllowed(CURRENCY_CODE)).thenReturn(false);
        when(bankInfoCache.getBankInfo(BANK_ID)).thenReturn(bankInfo);

        currencyManager.setupCurrency(CURRENCY_CODE, CURRENCY_SYMBOL, BANK_ID);
    }

    private static final class TestCurrencyManager extends CurrencyManager {
        private TestCurrencyManager(ApplicationContext context, CurrencyCache currencyCache, BankInfoCache bankInfoCache) {
            super(context, currencyCache, bankInfoCache);
        }
    }
}
