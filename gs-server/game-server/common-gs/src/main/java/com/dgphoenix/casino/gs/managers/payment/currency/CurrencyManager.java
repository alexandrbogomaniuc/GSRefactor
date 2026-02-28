package com.abs.casino.gs.managers.payment.currency;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraCurrencyPersister;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.abs.casino.common.currency.CurrencyRate;
import com.abs.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.exception.InvalidCurrencyRateException;
import com.abs.casino.common.exception.UnknownCurrencyException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.Pair;
import com.abs.casino.gs.persistance.remotecall.RemoteCallHelper;
import com.google.common.base.CharMatcher;
import org.springframework.context.ApplicationContext;

import java.util.Map;


/**
 * User: plastical
 * Date: 27.08.2010
 */
public class CurrencyManager {
    private static final double MAX_RATE = 50.0;
    private static final double MIN_RATE = 0.0009;
    private static CurrencyManager instance;
    private final CurrencyRatesManager currencyRatesManager;
    private final CassandraCurrencyPersister currencyPersister;
    private final RemoteCallHelper remoteCallHelper;
    private final CurrencyCache currencyCache;
    private final BankInfoCache bankInfoCache;

    public static CurrencyManager getInstance() {
        if (instance == null) {
            instance = new CurrencyManager();
        }
        return instance;
    }

    private CurrencyManager() {
        ApplicationContext context = ApplicationContextHelper.getApplicationContext();
        CassandraPersistenceManager persistenceManager = context.getBean(CassandraPersistenceManager.class);
        currencyPersister = persistenceManager.getPersister(CassandraCurrencyPersister.class);
        currencyRatesManager = context.getBean(CurrencyRatesManager.class);
        remoteCallHelper = context.getBean(RemoteCallHelper.class);
        currencyCache = CurrencyCache.getInstance();
        bankInfoCache = BankInfoCache.getInstance();
    }

    //for testing purpose
    protected CurrencyManager(ApplicationContext context, CurrencyCache currencyCache, BankInfoCache bankInfoCache) {
        this.currencyRatesManager = context.getBean(CurrencyRatesManager.class);
        CassandraPersistenceManager persistenceManager = context.getBean(CassandraPersistenceManager.class);
        this.currencyPersister = persistenceManager.getPersister(CassandraCurrencyPersister.class);
        this.remoteCallHelper = context.getBean(RemoteCallHelper.class);
        this.currencyCache = currencyCache;
        this.bankInfoCache = bankInfoCache;
    }

    public Currency setupCurrency(String currencyCode, String currencySymbol, long bankId) throws CommonException {
        if (!CharMatcher.ASCII.matchesAllOf(currencyCode)) {
            throw new CommonException("Cannot setupCurrency, currencyCode is not ASCII : " + currencyCode);
        }
        currencyCode = currencyCode.toUpperCase();
        Currency currency = currencyCache.get(currencyCode);
        if (currency == null) {
            throw new UnknownCurrencyException(currencyCode);
        }
        BankInfo bankInfo = bankInfoCache.getBankInfo(bankId);
        if (bankInfo == null) {
            throw new CommonException("Bank not found");
        }
        if (!bankInfo.isCurrencyCodeAllowed(currencyCode)) {
            throw new CommonException(" Currency is not allowed, currencyCode=" + currencyCode +
                    ", bankInfo=" + bankInfo.getId());
        }
        if (!bankInfo.isCurrencyExist(currency)) {
            if (!ICurrencyRateManager.DEFAULT_CURRENCY.equals(currencyCode)) {
                CurrencyRate rate = currencyRatesManager
                        .get(new Pair<>(currencyCode, ICurrencyRateManager.DEFAULT_CURRENCY));
                double rateValue = rate == null ? 1.0 : rate.getRate();
                if (rateValue == 1.0 || rateValue > MAX_RATE || rateValue < MIN_RATE) {
                    throw new InvalidCurrencyRateException(rate);
                }
            }

            bankInfo.addCurrency(currency);
        }
        return currency;
    }

    public Currency introduceCurrency(String currencyCode, String currencySymbol, long bankId) throws CommonException {
        BankInfo bankInfo = bankInfoCache.getBankInfo(bankId);
        Currency currency = currencyCache.get(currencyCode);
        if (currency == null) {
            currency = currencyCache.put(currencyCode, currencySymbol, bankId);
            currencyPersister.persist(currency.getCode(), currency);
            if (bankInfo != null) {
                remoteCallHelper.saveAndSendNotification(bankInfo);
            }
            currencyRatesManager.getRateToBaseCurrency(currencyCode);
        }
        return currency;
    }

    public String getCurrencyCodeByAlias(String currencyCode, BankInfo bankInfo) throws CommonException {
        if (currencyCache.isExist(currencyCode)) {
            return currencyCode;
        }
        Map<String, String> currencyAliases = bankInfo.getCurrencyAliases();
        if (currencyAliases.isEmpty()) {
            return currencyCode;
        }
        String currencyAlias = currencyAliases.get(currencyCode);
        if (currencyAlias == null) {
            throw new CommonException("Unknown currency alias: " + currencyCode);
        }
        if (!currencyCache.isExist(currencyAlias)) {
            throw new UnknownCurrencyException(currencyAlias);
        }
        return currencyAlias;
    }
}
