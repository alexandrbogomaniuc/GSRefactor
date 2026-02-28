package com.abs.casino.gs.persistance;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationType;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.abs.casino.common.transactiondata.TransactionData;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonGameWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation;
import junit.framework.TestCase;

/**
 * User: flsh
 * Date: 8/2/12
 */
public class CassandraTransactionDataPersisterTest extends TestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testSerializer() {
        //System.err.println("" + BaseGameInfo.class.isAssignableFrom(Identifiable.class));
        //System.err.println("" + Identifiable.class.isAssignableFrom(BaseGameInfo.class));
        //System.out.println("Integer.MAX_VALUE=" + Integer.MAX_VALUE);
        AccountInfo account = new AccountInfo(30000L, "454345", 21, (short) 10, System.currentTimeMillis(), false, false,
                new Currency("USD", "$"), "USA");
        ITransactionData data = new TransactionData(account.getLockId());
        data.setAccount(account);
        data.setLasthand(new LasthandInfo(1L, "uw97123jhiozsj   iojnjs sdf127892324o[ka,cm,cp[vosiopj12348u134iojpsd" +
                "kaklpawij9  klasdbva"));
        data.setGameSession(new GameSession(24234L, 30000L, 21L, 234L, System.currentTimeMillis(), 40000L,
                0L, 12, 15, false, true, new Currency("USD", "$"), "dfgdfgdfg", "EN", true, null));
        //data.setLastLockerId(1);
        data.setPlayerSession(new SessionInfo(30000L, ClientType.FLASH, "5353345", "local", "sdfxsdfaes2356" +
                "jfdssdgfsdf65", 1, System.currentTimeMillis(), null));
        final CommonWallet wallet = new CommonWallet();
        final CommonGameWallet gameWallet = new CommonGameWallet(1, 24234L);
        CommonWalletOperation betOp = new CommonWalletOperation(4444L, 30000L, 4365345L, 345L, 325345L,
                WalletOperationType.DEBIT, WalletOperationStatus.STARTED, WalletOperationStatus.STARTED,
                System.currentTimeMillis(), System.currentTimeMillis(), "dgdfgfd", "sfdsdfsdfsd", "4353453",
                1, null, 0, 0);
        gameWallet.setOperation(betOp);
        CommonWalletOperation winOp = new CommonWalletOperation(44446L, 30000L, 43653455L, 3457L, 32534566L,
                WalletOperationType.CREDIT, WalletOperationStatus.STARTED, WalletOperationStatus.STARTED,
                System.currentTimeMillis(), System.currentTimeMillis(), "dgdfgfd", "sfdsdfsdfsd", "4353453",
                1, null, 0, 0);
        gameWallet.setOperation(winOp);

        wallet.addGameWallet(gameWallet);
        data.setWallet(wallet);

        //final byte[] serialized = CassandraTransactionDataPersister.getInstance().serializeToBytes(data);
        //final byte[] serializedCompressed = CassandraTransactionDataPersister.getInstance().serializeToBytes(data);
        //System.out.println("serialized without compression: " + serialized.length + ", with=" +
        // serializedCompressed.length);
        //ITransactionData dataUncompressed = CassandraTransactionDataPersister.getInstance().
        //        deserializeFrom(serialized, TransactionData.class);
        //System.out.println("deserialized without compression: " + dataUncompressed);
        //ITransactionData dataCompressed = CassandraTransactionDataPersister.getInstance().
        //        deserializeFrom(serializedCompressed, TransactionData.class);
        //System.out.println("deserialized with compression: " + dataCompressed);
        //assertEquals(dataUncompressed.toString(), dataCompressed.toString());

    }
}
