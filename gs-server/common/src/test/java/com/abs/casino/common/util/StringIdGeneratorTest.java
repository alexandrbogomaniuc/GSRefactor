package com.abs.casino.common.util;

import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.string.StringIdGenerator;
import junit.framework.TestCase;

/**
 * User: flsh
 * Date: 4/13/12
 */
public class StringIdGeneratorTest extends TestCase {

    public void testSessionIdGenerator() {
        for (int i = 1; i < 100; i++) {
            AccountInfo account = new AccountInfo();
            account.setBankId(1);
            account.setExternalId(String.valueOf("user" + i));
            final String id = StringIdGenerator.generateSessionId(i,
                    account.getBankId(), account.getExternalId());
            final int parsed = StringIdGenerator.extractServerId(id);
            assertEquals(i, parsed);
        }
    }

    public void testUserHash() {
        for (int i = 1; i < 100; i++) {
            AccountInfo account = new AccountInfo();
            account.setBankId(1);
            account.setExternalId(String.valueOf("user//\\?-+_" + i));
            //account.setExternalId(String.valueOf("user_" + i));

            final String userHash = StringIdGenerator.getAccountHash(account.getBankId(), account.getExternalId());
            final String sessionId = StringIdGenerator.generateSessionId(i, account.getBankId(),
                    account.getExternalId());
            final String parsedUserHash = StringIdGenerator.extractUserHash(sessionId);
            final String decoded = StringIdGenerator.decodeFromXOR(userHash);
            Pair<Integer, String> pair = StringIdGenerator.extractBankAndExternalUserId(sessionId);
            //System.out.println("sessionId=" + sessionId + ", userHash=" + userHash + ", parsedUserHash=" +
            //        parsedUserHash + " decoded=" + decoded + ", pair=" + pair);
            assertEquals(userHash, parsedUserHash);
        }
    }

}
