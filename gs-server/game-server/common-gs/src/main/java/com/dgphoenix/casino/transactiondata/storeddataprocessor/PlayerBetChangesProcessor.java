package com.abs.casino.transactiondata.storeddataprocessor;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraBetPersister;
import com.abs.casino.cassandra.persist.IStoredDataProcessor;
import com.abs.casino.common.cache.data.bet.PlayerBet;
import com.abs.casino.common.transactiondata.storeddate.StoredItem;
import com.abs.casino.common.transactiondata.storeddate.identifier.PlayerBetStoredInfo;
import com.abs.casino.common.util.ApplicationContextHelper;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

/**
 * User: Grien
 * Date: 19.12.2014 16:48
 */
public class PlayerBetChangesProcessor implements IStoredDataProcessor<PlayerBet, PlayerBetStoredInfo> {
    private final CassandraBetPersister betPersister;

    public PlayerBetChangesProcessor() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        betPersister = persistenceManager.getPersister(CassandraBetPersister.class);
    }

    @Override
    public void process(StoredItem<PlayerBet, PlayerBetStoredInfo> item, HashMap<com.abs.casino.cassandra.persist.engine.Session, List<com.datastax.driver.core.Statement>> statementsMap, List<ByteBuffer> byteBuffersCollector) {
        betPersister.prepareToPersistBet(statementsMap, item.getIdentifier().getGameSessionId(), item.getItem(), byteBuffersCollector);
    }
}
