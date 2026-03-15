package com.abs.casino.transactiondata.storeddataprocessor;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraAccountInfoPersister;
import com.abs.casino.cassandra.persist.IStoredDataProcessor;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.transactiondata.storeddate.StoredItem;
import com.abs.casino.common.transactiondata.storeddate.identifier.StoredItemInfo;
import com.abs.casino.common.util.ApplicationContextHelper;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

/**
 * User: Grien
 * Date: 19.12.2014 15:21
 */
public class AccountChangesProcessor implements IStoredDataProcessor<AccountInfo, StoredItemInfo<AccountInfo>> {
    private final CassandraAccountInfoPersister accountInfoPersister;

    public AccountChangesProcessor() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        accountInfoPersister = persistenceManager.getPersister(CassandraAccountInfoPersister.class);
    }

    @Override
    public void process(StoredItem<AccountInfo, StoredItemInfo<AccountInfo>> item,
                        HashMap<com.abs.casino.cassandra.persist.engine.Session, List<com.datastax.driver.core.Statement>> statementsMap, List<ByteBuffer> byteBuffersCollector) {
        accountInfoPersister.prepareToPersist(statementsMap, item.getItem(),
                byteBuffersCollector);
    }
}
