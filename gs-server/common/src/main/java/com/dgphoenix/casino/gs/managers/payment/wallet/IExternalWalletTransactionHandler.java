package com.abs.casino.gs.managers.payment.wallet;

import com.abs.casino.common.exception.WalletException;
import com.dgphoenix.casino.gs.managers.payment.wallet.IWalletOperation;

/**
 * User: flsh
 * Date: 14.11.13
 */
public interface IExternalWalletTransactionHandler {
    void operationCreated(IWalletOperation operation) throws WalletException;

    void operationCompleted(IWalletOperation operation, long gameId) throws WalletException;
}
