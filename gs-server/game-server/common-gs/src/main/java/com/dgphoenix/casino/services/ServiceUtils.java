package com.abs.casino.services;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;

/**
 * @author <a href="mailto:dader@dgphoenix.com">Timur Shaymardanov</a>
 * @since 13.02.2020
 */
public class ServiceUtils {

    private final BankInfoCache bankInfoCache;
    private final CurrencyCache currencyCache;

    public ServiceUtils(BankInfoCache bankInfoCache, CurrencyCache currencyCache) {
        this.bankInfoCache = bankInfoCache;
        this.currencyCache = currencyCache;
    }

    public Currency retrieveCurrency(long bankId, String currencyCode) throws CommonException {
        if (StringUtils.isTrimmedEmpty(currencyCode)) {
            throw new CommonException("invalid currency");
        }
        if (!currencyCache.isExist(currencyCode)) {
            throw new CommonException("currency doesn't exist");
        }
        Currency currency = currencyCache.get(currencyCode);
        if (!bankInfoCache.isExist(bankId)) {
            throw new CommonException("bank doesn't exist");
        }
        BankInfo bankInfo = bankInfoCache.getBankInfo(bankId);
        if (!bankInfo.isCurrencyExist(currency)) {
            throw new CommonException("currency is not supported");
        }
        return currency;
    }

    public static Currency getCurrency(long bankId, String currencyCode) throws CommonException {
        ServiceUtils serviceUtils = new ServiceUtils(BankInfoCache.getInstance(), CurrencyCache.getInstance());
        return serviceUtils.retrieveCurrency(bankId, currencyCode);
    }

}
