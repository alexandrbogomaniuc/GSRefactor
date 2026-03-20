package com.abs.casino.transactiondata.storeddataprocessor;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.IStoredDataProcessor;
import com.abs.casino.common.promo.PromoCampaignMemberInfos;
import com.abs.casino.common.transactiondata.storeddate.StoredItem;
import com.abs.casino.common.transactiondata.storeddate.identifier.StoredItemInfo;
import com.abs.casino.common.util.ApplicationContextHelper;
import com.abs.casino.promo.persisters.CassandraPromoCampaignMembersPersister;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

/**
 * User: flsh
 * Date: 12.01.17.
 */
public class PromoCampaignMembersChangesProcessor
        implements IStoredDataProcessor<PromoCampaignMemberInfos, StoredItemInfo<PromoCampaignMemberInfos>> {
    private final CassandraPromoCampaignMembersPersister promoCampaignMembersPersister;

    public PromoCampaignMembersChangesProcessor() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        promoCampaignMembersPersister = persistenceManager.getPersister(CassandraPromoCampaignMembersPersister.class);
    }

    @Override
    public void process(StoredItem<PromoCampaignMemberInfos, StoredItemInfo<PromoCampaignMemberInfos>> item,
                        HashMap<com.abs.casino.cassandra.persist.engine.Session, List<com.datastax.driver.core.Statement>> statementsMap, List<ByteBuffer> byteBuffersCollector) {
        promoCampaignMembersPersister.prepareToPersist(statementsMap, item.getItem(), byteBuffersCollector);
    }
}
