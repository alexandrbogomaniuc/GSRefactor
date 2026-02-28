package com.abs.casino.common.transactiondata;

import com.dgphoenix.casino.common.transactiondata.ITransactionData;

/**
 * User: flsh
 * Date: 20.02.15.
 */
public interface ITransactionDataProcessor {
    void process(String lockId, TrackingState state, TrackingInfo trackingInfo, ITransactionData cachedValue);

    boolean isStopProcessing();
}
