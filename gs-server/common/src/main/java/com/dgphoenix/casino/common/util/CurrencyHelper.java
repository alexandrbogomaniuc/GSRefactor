package com.dgphoenix.casino.common.util;

import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.currency.Currency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: plastical
 * Date: 16.08.2010
 */
public class CurrencyHelper {
    private static final CurrencyHelper instance = new CurrencyHelper();

    public static CurrencyHelper getInstance() {
        return instance;
    }

    private CurrencyHelper() {
    }

    public Currency getCurrency(long bankId) {
        int iBankId = (int) bankId;

        List<Integer> usdBanks = new ArrayList<Integer>();
        List<Integer> sbBanks =
                Arrays.asList(3, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
                        31, 32, 33, 34, 48, 49, 50, 51, 52, 53, 54, 55, 56);
        List<Integer> wwBanks = Arrays.asList(43, 44, 45, 41);
        List<Integer> otherUSDBanks = Arrays.asList(6, 7);
        usdBanks.addAll(sbBanks);
        usdBanks.addAll(wwBanks);
        usdBanks.addAll(otherUSDBanks);

        if (usdBanks.contains(iBankId)) {
            return CurrencyCache.getInstance().get("USD");
        }


        List<Integer> euroBanks = new ArrayList<Integer>();
        List<Integer> pwBank = Arrays.asList(4);
        List<Integer> upEURBanks = Arrays.asList(40, 35, 36, 42, 38, 59);
        List<Integer> upr = Arrays.asList(47);
        euroBanks.addAll(pwBank);
        euroBanks.addAll(upEURBanks);
        euroBanks.addAll(upr);

        if (euroBanks.contains(iBankId)) {
            return CurrencyCache.getInstance().get("EUR");
        }


        List<Integer> gbpBanks = new ArrayList<Integer>();
        List<Integer> slBank = Arrays.asList(8);
        gbpBanks.addAll(slBank);

        if (gbpBanks.contains(iBankId)) {
            return CurrencyCache.getInstance().get("GBP");
        }


        List<Integer> liraBanks = new ArrayList<Integer>();
        List<Integer> upLira = Arrays.asList(37);
        liraBanks.addAll(upLira);

        if (liraBanks.contains(iBankId)) {
            return CurrencyCache.getInstance().get("TRY");
        }


        return CurrencyCache.getInstance().get("EUR");
    }
}
