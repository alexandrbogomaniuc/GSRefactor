package com.abs.casino.transactiondata.storeddataprocessor;

import com.dgphoenix.casino.cassandra.persist.IStoredDataProcessor;
import com.abs.casino.common.transactiondata.storeddate.StoredItem;
import com.abs.casino.common.transactiondata.storeddate.identifier.PlayerBetTransferStoredInfo;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.abs.casino.gs.persistance.bet.PlayerBetPersistenceManager;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

/**
 * User: Grien
 * Date: 19.12.2014 16:01
 */
public class PlayerBetTransferProcessor implements IStoredDataProcessor<Long, PlayerBetTransferStoredInfo> {
    private final PlayerBetPersistenceManager betPersistenceManager;

    public PlayerBetTransferProcessor() {
        betPersistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("playerBetPersistenceManager", PlayerBetPersistenceManager.class);
    }

    @Override
    public void process(StoredItem<Long, PlayerBetTransferStoredInfo> item,
                        HashMap<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap, List<ByteBuffer> byteBuffersCollector) {
        Long gameSessionId = item.getItem();
        betPersistenceManager.prepareToPersistGameSessionBets(statementsMap, gameSessionId,
                item.getIdentifier().getMaxPlayerBetId(), byteBuffersCollector);
    }
}
