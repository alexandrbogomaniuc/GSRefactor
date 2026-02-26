package com.dgphoenix.casino.transactiondata.storeddataprocessor;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraGameSessionPersister;
import com.dgphoenix.casino.cassandra.persist.IStoredDataProcessor;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItem;
import com.dgphoenix.casino.common.transactiondata.storeddate.identifier.StoredItemInfo;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

/**
 * User: Grien
 * Date: 19.12.2014 15:21
 */
public class GameSessionHistoryChangesProcessor implements IStoredDataProcessor<GameSession, StoredItemInfo<GameSession>> {
    private final CassandraGameSessionPersister gameSessionPersister;

    public GameSessionHistoryChangesProcessor() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        gameSessionPersister = persistenceManager.getPersister(CassandraGameSessionPersister.class);
    }

    @Override
    public void process(StoredItem<GameSession, StoredItemInfo<GameSession>> item, HashMap<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap, List<ByteBuffer> byteBuffersCollector) {
        gameSessionPersister.prepareToPersist(statementsMap, item.getItem());
    }
}
