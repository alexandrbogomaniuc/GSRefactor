package com.abs.casino.gs.managers.payment.wallet;

import com.dgphoenix.casino.common.cache.data.payment.IWallet;

/**
 * User: flsh
 * Date: 12.02.15.
 */
public interface IWalletPersister {
    public IWallet getWallet(long accountId);

    void removeWallet(long accountId);

    void removeGameWallet(long accountId, int gameId);
}
