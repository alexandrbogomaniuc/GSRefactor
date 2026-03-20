package com.abs.casino.transactiondata.storeddataprocessor;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraPaymentTransactionPersister;
import com.abs.casino.cassandra.persist.IStoredDataProcessor;
import com.abs.casino.common.cache.data.payment.transfer.PaymentTransaction;
import com.abs.casino.common.transactiondata.storeddate.StoredItem;
import com.abs.casino.common.transactiondata.storeddate.identifier.StoredItemInfo;
import com.abs.casino.common.util.ApplicationContextHelper;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

/**
 * User: flsh
 * Date: 22.08.15.
 */
public class PaymentTransactionChangesProcessor implements IStoredDataProcessor<PaymentTransaction, StoredItemInfo<PaymentTransaction>> {
    private final CassandraPaymentTransactionPersister paymentTransactionPersister;

    public PaymentTransactionChangesProcessor() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        paymentTransactionPersister = persistenceManager.getPersister(CassandraPaymentTransactionPersister.class);
    }

    @Override
    public void process(StoredItem<PaymentTransaction, StoredItemInfo<PaymentTransaction>> item,
                        HashMap<com.abs.casino.cassandra.persist.engine.Session, List<com.datastax.driver.core.Statement>> statementsMap, List<ByteBuffer> byteBuffersCollector) {
        paymentTransactionPersister.prepareToPersist(statementsMap, item.getItem(),
                byteBuffersCollector);
    }
}
