package com.dgphoenix.casino.gs.managers.payment.wallet;

import com.dgphoenix.casino.common.exception.WalletException;

/**
 * User: flsh
 * Date: 14.11.13
 */
public interface IExternalWalletTransactionHandler {
    void operationCreated(IWalletOperation operation) throws WalletException;

    void operationCompleted(IWalletOperation operation, long gameId) throws WalletException;
}
