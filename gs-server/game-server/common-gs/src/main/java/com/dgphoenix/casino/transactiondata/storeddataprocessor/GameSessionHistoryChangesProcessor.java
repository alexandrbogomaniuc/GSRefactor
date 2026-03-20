package com.abs.casino.transactiondata.storeddataprocessor;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraGameSessionPersister;
import com.abs.casino.cassandra.persist.IStoredDataProcessor;
import com.abs.casino.common.cache.data.session.GameSession;
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
public class GameSessionHistoryChangesProcessor implements IStoredDataProcessor<GameSession, StoredItemInfo<GameSession>> {
    private final CassandraGameSessionPersister gameSessionPersister;

    public GameSessionHistoryChangesProcessor() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        gameSessionPersister = persistenceManager.getPersister(CassandraGameSessionPersister.class);
    }

    @Override
    public void process(StoredItem<GameSession, StoredItemInfo<GameSession>> item, HashMap<com.abs.casino.cassandra.persist.engine.Session, List<com.datastax.driver.core.Statement>> statementsMap, List<ByteBuffer> byteBuffersCollector) {
        gameSessionPersister.prepareToPersist(statementsMap, item.getItem());
    }
}
