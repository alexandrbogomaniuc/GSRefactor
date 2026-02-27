package com.abs.casino.common.util;

import com.dgphoenix.casino.common.util.AccountIdGenerator;
import junit.framework.TestCase;

/**
 * User: flsh
 * Date: 12/28/11
 */
public class AccountIdGeneratorTest extends TestCase {
    private static class AccountIdGeneratorAccessor extends AccountIdGenerator {
        private static long generateComposedValue(long bankId, long accountId) {
            return generateComposed(bankId, accountId);
        }
    }

    public void testTmp() {
        String key = "465+55";
        final String accountId = key.substring(0, key.indexOf("+"));
        //System.out.println("accountId=" + accountId);
/*
        final String maxValue = Long.toBinaryString(Long.MAX_VALUE);
        //System.out.println("maxValue in bin: " + maxValue);
        final long maxLong = Long.parseLong("111111111111111111111111111111111111111111111111111111111111111", 2);
        //001 oneHigh - pm-real
        final long oneHigh = Long.parseLong("001000000000000000000000000000000000000000000000000000000000000", 2);

        //010 twoHigh - pm1 free
        final long twoHigh = Long.parseLong("010000000000000000000000000000000000000000000000000000000000000", 2);
        System.out.println("maxLong in bin: " + maxLong);
        System.out.println("oneHigh in bin: " + oneHigh);
        System.out.println("twoHigh in bin: " + twoHigh);
*/
    }

    public void testGoodId() {
        long id = AccountIdGeneratorAccessor.generateComposedValue(1, 2251799813685247L);
        assertEquals(0xfffffffffffffL, id);

        id = AccountIdGeneratorAccessor.generateComposedValue(0xffe, 1l);
        assertEquals(0x7ff0000000000001L, id);

        id = AccountIdGeneratorAccessor.generateComposedValue(0xfff, 2251799813685247L);
        assertEquals(0x7fffffffffffffffL, id);

        id = AccountIdGeneratorAccessor.generateComposedValue(1, 1);
        assertEquals(0x8000000000001L, id);
    }

    public void testBadBankId() {
        boolean thrown = false;
        try {
            AccountIdGeneratorAccessor.generateComposedValue(4096, 10);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

        thrown = false;
        try {
            AccountIdGeneratorAccessor.generateComposedValue(0, 10);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    public void testBadAccountId() {
        boolean thrown = false;
        try {
            AccountIdGeneratorAccessor.generateComposedValue(1, 2251799813685248L);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

        thrown = false;
        try {
            AccountIdGeneratorAccessor.generateComposedValue(1, 0);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

}
