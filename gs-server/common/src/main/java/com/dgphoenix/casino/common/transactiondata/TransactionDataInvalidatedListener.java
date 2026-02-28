package com.abs.casino.common.transactiondata;

import com.dgphoenix.casino.common.transactiondata.ITransactionData;

/**
 * User: flsh
 * Date: 02.10.17.
 */
public interface TransactionDataInvalidatedListener {
    void invalidate(ITransactionData td);
}
