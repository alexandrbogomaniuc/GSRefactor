package com.abs.casino.transactiondata;

import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.abs.casino.common.transactiondata.ITransactionDataCreator;
import com.abs.casino.common.transactiondata.TransactionData;

/**
 * User: Grien
 * Date: 30.10.2014 5:49
 */
public class BasicTransactionDataCreator implements ITransactionDataCreator {
    @Override
    public ITransactionData create(String lockId, int gameServerId) {
        TransactionData transactionData = new TransactionData(lockId);
        return transactionData;
    }
}
