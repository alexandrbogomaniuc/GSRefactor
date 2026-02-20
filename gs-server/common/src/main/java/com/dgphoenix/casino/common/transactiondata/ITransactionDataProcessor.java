package com.dgphoenix.casino.common.transactiondata;

/**
 * User: flsh
 * Date: 20.02.15.
 */
public interface ITransactionDataProcessor {
    void process(String lockId, TrackingState state, TrackingInfo trackingInfo, ITransactionData cachedValue);

    boolean isStopProcessing();
}
