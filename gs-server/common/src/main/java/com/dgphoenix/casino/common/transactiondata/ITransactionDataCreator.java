package com.dgphoenix.casino.common.transactiondata;

/**
 * User: Grien
 * Date: 10.06.2014 16:30
 */
public interface ITransactionDataCreator {
    ITransactionData create(String lockId, int gameServerId);
}
