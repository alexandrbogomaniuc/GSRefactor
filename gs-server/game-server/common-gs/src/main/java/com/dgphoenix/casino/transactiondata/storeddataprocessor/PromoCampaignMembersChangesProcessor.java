package com.dgphoenix.casino.transactiondata.storeddataprocessor;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.IStoredDataProcessor;
import com.dgphoenix.casino.common.promo.PromoCampaignMemberInfos;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItem;
import com.dgphoenix.casino.common.transactiondata.storeddate.identifier.StoredItemInfo;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.promo.persisters.CassandraPromoCampaignMembersPersister;

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
                        HashMap<Session, List<Statement>> statementsMap, List<ByteBuffer> byteBuffersCollector) {
        promoCampaignMembersPersister.prepareToPersist(statementsMap, item.getItem(), byteBuffersCollector);
    }
}
