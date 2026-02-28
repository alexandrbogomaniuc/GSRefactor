package com.abs.casino.transactiondata.storeddataprocessor;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraAccountInfoPersister;
import com.dgphoenix.casino.cassandra.persist.IStoredDataProcessor;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.transactiondata.storeddate.StoredItem;
import com.abs.casino.common.transactiondata.storeddate.identifier.StoredItemInfo;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;

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
                        HashMap<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap, List<ByteBuffer> byteBuffersCollector) {
        accountInfoPersister.prepareToPersist(statementsMap, item.getItem(),
                byteBuffersCollector);
    }
}
