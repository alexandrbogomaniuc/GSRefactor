package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItem;
import com.dgphoenix.casino.common.transactiondata.storeddate.identifier.StoredItemInfo;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

/**
 * User: Grien
 * Date: 19.12.2014 15:50
 */
public interface IStoredDataProcessor<T, I extends StoredItemInfo<T>> {
    void process(StoredItem<T, I> item, HashMap<Session, List<Statement>> statementsMap, List<ByteBuffer> byteBuffersCollector);
}
