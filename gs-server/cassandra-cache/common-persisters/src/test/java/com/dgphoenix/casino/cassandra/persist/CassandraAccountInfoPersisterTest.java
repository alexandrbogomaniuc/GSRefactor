package com.dgphoenix.casino.cassandra.persist;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * User: flsh
 * Date: 25.05.13
 */
public class CassandraAccountInfoPersisterTest extends TestCase {

    @Test
    public void testSerialization() {
        testAccount(new AccountInfo());

        AccountInfo accountInfo = new AccountInfo(30000L, "454345", 21, (short) 10, System.currentTimeMillis(), true,
                false, new Currency("USD", "$"), "USA");
        testAccount(accountInfo);

        accountInfo.setFinsoftSessionId("ffff");
        accountInfo.setSmartLiveOperator("ssss");
        accountInfo.setAccountUseId(3L);
        accountInfo.setAgentId("ag");
        accountInfo.setEmail("aaa@mail.com");
        accountInfo.setFirstName("first");
        accountInfo.setLastName("last");
        accountInfo.setPassword("pass");
        accountInfo.setSessionKey("session");
        //accountInfo.setBonusIdsList("10");
        //accountInfo.setFrBonusIdsList("12");
        //accountInfo.setFrbMassAwardIdsList("6666");
        accountInfo.setNickName("nick");
        //accountInfo.incrementVersion();
/*        accountInfo.setLastHand("FRB+274+48801599", "FSC=1&amp;IBETVALUES=0.02 0.05 0.1 0.25 0.5 1.0&amp;" +
                "LASTSTOPREEL=10|2|6|3|11|8|8|4|1|5|4|0|10|4|6&amp;" +
                "STATE=0&amp;LASTBET=2|30|1%MODERATOR=1.0|0.97|3.0|0.0|0.0|0.0&amp;TPAY=0.0%%");
        accountInfo.setLastHand("FRB+274+48843055", "FSC=2&amp;IBETVALUES=0.02 0.05 0.1 0.25 0.5 1.0&amp;" +
                "LASTSTOPREEL=2|6|6|8|6|6|2|7|3|11|8|8|8|8|6&amp;STATE=0&amp;" +
                "LASTBET=2|30|1%MODERATOR=1.0|0.97|3.0|0.0|0.0|0.0&amp;TPAY=0.0%%");*/
        testAccount(accountInfo);
    }


    private void testAccount(AccountInfo accountInfo) {
        //byte[] bytes = CassandraAccountInfoPersister.getInstance().serializeToBytes(accountInfo);
        //System.out.println("testAccount: size=" + bytes.length);
        //AccountInfo deserializedAccount = CassandraAccountInfoPersister.getInstance().deserializeFrom(bytes,
        //        AccountInfo.class);
        //assertEquals(accountInfo, deserializedAccount);
        //assertEquals(accountInfo.toString(), deserializedAccount.toString());
    }
}
