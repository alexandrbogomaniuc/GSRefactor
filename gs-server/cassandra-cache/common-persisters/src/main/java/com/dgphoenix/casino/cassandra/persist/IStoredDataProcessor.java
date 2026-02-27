package com.dgphoenix.casino.cassandra.persist;

import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItem;
import com.abs.casino.common.transactiondata.storeddate.identifier.StoredItemInfo;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

/**
 * User: Grien
 * Date: 19.12.2014 15:50
 */
public interface IStoredDataProcessor<T, I extends StoredItemInfo<T>> {
    void process(StoredItem<T, I> item, HashMap<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap, List<ByteBuffer> byteBuffersCollector);
}
