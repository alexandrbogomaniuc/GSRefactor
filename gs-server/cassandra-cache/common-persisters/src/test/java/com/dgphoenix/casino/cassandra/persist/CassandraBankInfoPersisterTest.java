package com.abs.casino.cassandra.persist;

import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.bank.Coin;
import com.abs.casino.common.cache.data.bank.Limit;
import com.abs.casino.common.cache.data.currency.Currency;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * User: flsh
 * Date: 30.08.13
 */
public class CassandraBankInfoPersisterTest extends TestCase {

    @Test
    public void testSerialization() {
        long subCasinoId = 37l;
        String subCasinoName = "Sis test";
        int bankId = 160;
        Currency currency = new Currency("USD", "\u0024");
        Limit limit = Limit.valueOf(100, 10000);
        List<Coin> coins = new ArrayList<Coin>();
        coins.add(Coin.getByValue(10));
        coins.add(Coin.getByValue(50));
        coins.add(Coin.getByValue(100));

        BankInfo bank = new BankInfo(bankId, String.valueOf(bankId), "Sis", currency, limit, coins);
        bank.setPersistBets(true);
        bank.setPersistAccounts(true);
        bank.setPersistGameSessions(true);
        bank.setPersistPlayerSessions(false);
        bank.setPersistWalletOps(false);
        bank.setSubCasinoId(subCasinoId);

        bank.setProperty(BankInfo.KEY_PSM_CLASS, "com.dgphoenix.casino.slotstories.sm.SlotsStoriesPlayerSessionManager");
        bank.setProperty(BankInfo.KEY_REPLACE_START_GS_FROM, "games");
        bank.setProperty(BankInfo.KEY_REPLACE_START_GS_TO, "gss");

        bank.setProperty(BankInfo.KEY_TRANSACTION_DATA_CLASS, "com.dgphoenix.casino.slotsstories.transactiondata.TransactionDataStorageHelper");
        bank.setProperty(BankInfo.KEY_EXTENDED_GAMEPLAY_PROCESSOR, "com.dgphoenix.casino.slotsstories.game.ExtendedGameplayProcessor");
        //byte[] bytes = CassandraBankInfoPersister.getInstance().serializeToBytes(bank);

        //BankInfo deserialized = CassandraBankInfoPersister.getInstance().deserializeFrom(bytes, BankInfo.class);
        //assertEquals(bank, deserialized);
        //assertEquals(bank.toString(), deserialized.toString());
        //System.out.println(deserialized);
    }
}
