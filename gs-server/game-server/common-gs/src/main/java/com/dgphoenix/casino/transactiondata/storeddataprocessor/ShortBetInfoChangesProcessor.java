package com.abs.casino.transactiondata.storeddataprocessor;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraShortBetInfoPersister;
import com.dgphoenix.casino.cassandra.persist.IStoredDataProcessor;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bet.ShortBetInfo;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItem;
import com.dgphoenix.casino.common.transactiondata.storeddate.identifier.StoredItemInfo;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

/**
 * User: flsh
 * Date: 07.03.17.
 */
public class ShortBetInfoChangesProcessor implements IStoredDataProcessor<ShortBetInfo, StoredItemInfo<ShortBetInfo>> {
    private CassandraShortBetInfoPersister shortBetInfoPersister;
    private BankInfoCache bankInfoCache;

    public ShortBetInfoChangesProcessor(BankInfoCache bankInfoCache) {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        shortBetInfoPersister = persistenceManager.getPersister(CassandraShortBetInfoPersister.class);
        this.bankInfoCache = bankInfoCache;
    }

    @Override
    public void process(StoredItem<ShortBetInfo, StoredItemInfo<ShortBetInfo>> item,
                        HashMap<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap, List<ByteBuffer> byteBuffersCollector) {
        BankInfo bankInfo = bankInfoCache.getBankInfo(item.getItem().getBankId());
        shortBetInfoPersister.prepareToPersist(statementsMap, item.getItem(), byteBuffersCollector, bankInfo.getShortBetInfoTtl());
    }
}
