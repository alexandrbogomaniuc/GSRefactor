package com.abs.casino.transactiondata.storeddataprocessor;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraPaymentTransactionPersister;
import com.dgphoenix.casino.cassandra.persist.IStoredDataProcessor;
import com.dgphoenix.casino.common.cache.data.payment.transfer.PaymentTransaction;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItem;
import com.abs.casino.common.transactiondata.storeddate.identifier.StoredItemInfo;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;

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
                        HashMap<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap, List<ByteBuffer> byteBuffersCollector) {
        paymentTransactionPersister.prepareToPersist(statementsMap, item.getItem(),
                byteBuffersCollector);
    }
}
