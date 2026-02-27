package com.abs.casino.common.cache;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.CurrencyRateMultiplierContainer;
import com.dgphoenix.casino.common.cache.CurrencyRateMultiplierLoader;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CurrencyRateMultiplierLoaderTest {

    private static final String VND = "VND";
    private static final long BANK_ID = 777L;

    @Mock
    private BankInfoCache bankInfoCache;
    @Mock
    private BankInfo bankInfo;

    private CurrencyRateMultiplierLoader loader;

    @Before
    public void setUp() throws Exception {
        when(bankInfoCache.getBankInfo(BANK_ID)).thenReturn(bankInfo);
        loader = new CurrencyRateMultiplierLoader(bankInfoCache);
    }

    @Test
    public void loadForIncorrectBankId() {
        CurrencyRateMultiplierContainer container = loader.load(123L);

        assertEquals(1, container.getMultiplier(VND));
    }

    @Test
    public void loadEmptyMultipliers() {
        CurrencyRateMultiplierContainer container = loader.load(BANK_ID);

        assertEquals(1, container.getMultiplier(VND));
    }

    @Test
    public void loadContainerWithOneMultiplier() {
        Map<String, String> multipliers = new HashMap<>();
        multipliers.put(VND, "100");
        when(bankInfo.getCurrencyRateMultipliers()).thenReturn(multipliers);

        CurrencyRateMultiplierContainer container = loader.load(BANK_ID);

        assertEquals(100, container.getMultiplier(VND));
        assertEquals(1, container.getMultiplier("JPY"));
    }

    @Test
    public void loadContainerWithManyMultipliers() {
        Map<String, String> multipliers = new HashMap<>();
        multipliers.put(VND, "1000");
        multipliers.put("KRW", "NaN");
        multipliers.put("IDR", "0");
        multipliers.put("CNY", "-10");
        multipliers.put("JPY", "10");
        when(bankInfo.getCurrencyRateMultipliers()).thenReturn(multipliers);

        CurrencyRateMultiplierContainer container = loader.load(BANK_ID);
        assertEquals(1000, container.getMultiplier(VND));
        assertEquals(10, container.getMultiplier("JPY"));
        assertEquals(1, container.getMultiplier("KRW"));
        assertEquals(1, container.getMultiplier("IDR"));
        assertEquals(1, container.getMultiplier("CNY"));
    }
}
