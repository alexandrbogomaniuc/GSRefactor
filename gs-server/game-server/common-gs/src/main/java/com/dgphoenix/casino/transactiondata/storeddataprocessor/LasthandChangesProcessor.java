package com.abs.casino.transactiondata.storeddataprocessor;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraLasthandPersister;
import com.abs.casino.cassandra.persist.IStoredDataProcessor;
import com.abs.casino.common.cache.data.account.LasthandInfo;
import com.abs.casino.common.transactiondata.storeddate.StoredItem;
import com.abs.casino.common.transactiondata.storeddate.identifier.LasthandStoredInfo;
import com.abs.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.util.string.StringUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

/**
 * User: Grien
 * Date: 19.12.2014 17:01
 */
public class LasthandChangesProcessor implements IStoredDataProcessor<LasthandInfo, LasthandStoredInfo> {
    private final CassandraLasthandPersister lasthandPersister;

    public LasthandChangesProcessor() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        lasthandPersister = persistenceManager.getPersister(CassandraLasthandPersister.class);
    }

    @Override
    public void process(StoredItem<LasthandInfo, LasthandStoredInfo> item, HashMap<com.abs.casino.cassandra.persist.engine.Session, List<com.datastax.driver.core.Statement>> statementsMap, List<ByteBuffer> byteBuffersCollector) {
        LasthandInfo lasthandInfo = item.getItem();
        LasthandStoredInfo k = item.getIdentifier();
        if (lasthandInfo == null || StringUtils.isTrimmedEmpty(lasthandInfo.getLasthandData())) {
            lasthandPersister.prepareToDeletion(statementsMap, k.getAccountId(), k.getGameId(), k.getBonusId(), k.getBonusSystemType());
        } else {
            lasthandPersister.prepareToPersist(statementsMap, k.getAccountId(), k.getGameId(), k.getBonusId(), lasthandInfo.getLasthandData(), k.getBonusSystemType());
        }
    }
}
