package com.abs.casino.transactiondata.storeddataprocessor;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.IStoredDataProcessor;
import com.dgphoenix.casino.common.promo.TournamentMemberRanks;
import com.abs.casino.common.transactiondata.storeddate.StoredItem;
import com.abs.casino.common.transactiondata.storeddate.identifier.StoredItemInfo;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.abs.casino.promo.persisters.CassandraTournamentRankPersister;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

/**
 * User: flsh
 * Date: 12.01.17.
 */
public class PromoTournamentRankChangesProcessor
        implements IStoredDataProcessor<TournamentMemberRanks, StoredItemInfo<TournamentMemberRanks>> {
    private final CassandraTournamentRankPersister tournamentRankPersister;

    public PromoTournamentRankChangesProcessor() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        tournamentRankPersister = persistenceManager.getPersister(CassandraTournamentRankPersister.class);
    }

    @Override
    public void process(StoredItem<TournamentMemberRanks, StoredItemInfo<TournamentMemberRanks>> item,
                        HashMap<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap, List<ByteBuffer> byteBuffersCollector) {
        tournamentRankPersister.prepareToPersist(statementsMap, item.getItem(), byteBuffersCollector);
    }
}
