package com.abs.casino.common.transactiondata;

import com.dgphoenix.casino.common.transactiondata.ITransactionData;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * User: Grien
 * Date: 30.05.2014 18:46
 */
public interface ITransactionDataStorageHelper {
    ITransactionData create(String lockId, Map<String, ByteBuffer> map, int lastLockerId);

    Map<String, ByteBuffer> getStoredData(ITransactionData data);

    ITDFieldSerializeHelper getHelper(String field);

    public interface ITDFieldSerializeHelper {
        public abstract ByteBuffer serialize(Object o);

        public abstract <T> T deserialize(ByteBuffer buffer);
    }
}
