package com.dgphoenix.casino.common.cache;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BankPartnerIdCacheTest {
    private final BankPartnerIdCache partnerIdCache = new BankPartnerIdCache();

    @Before
    public void setUp() throws Exception {
        partnerIdCache.put("134", 271);
    }

    @Test
    public void getBankId() {
        Integer bankId = partnerIdCache.getBankId("134");

        assertEquals(271, (int) bankId);
    }

    @Test
    public void whenChangeToNull() {
        partnerIdCache.put(null, 271);
        Integer bankId = partnerIdCache.getBankId("134");

        assertNull(bankId);
    }

    @Test
    public void whenChangeToDifferValue() {
        partnerIdCache.put("135", 271);
        Integer byNew = partnerIdCache.getBankId("135");
        Integer byOld = partnerIdCache.getBankId("134");

        assertEquals(271, (int) byNew);
        assertNull(byOld);
    }

    @Test
    public void whenAddNull() {
        partnerIdCache.put(null, 5554);
    }

    @Test
    public void whenPutNewValue() {
        partnerIdCache.put("135", 5554);
        Integer bankId = partnerIdCache.getBankId("135");

        assertEquals(5554, (int) bankId);
    }

    @Test
    public void remove() {
        partnerIdCache.remove(271);

        Integer bankId = partnerIdCache.getBankId("134");
        assertNull(bankId);
    }
}