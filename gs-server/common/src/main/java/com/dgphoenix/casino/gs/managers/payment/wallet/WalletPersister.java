package com.abs.casino.gs.managers.payment.wallet;

import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.CacheKeyInfo;
import com.dgphoenix.casino.common.cache.IDistributedCache;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;

/**
 * User: flsh
 * Date: 4/26/12
 */

@CacheKeyInfo(description = "persister.accountId")
public class WalletPersister implements IDistributedCache<String, IWallet> {
    private static final WalletPersister instance = new WalletPersister();
    private static final Logger LOG = LogManager.getLogger(WalletPersister.class);
    private IWalletPersister persister;

    private WalletPersister() {
    }

    public static WalletPersister getInstance() {
        return instance;
    }

    public void init(IWalletPersister persister) {
        this.persister = persister;
    }

    public void setWallet(long accountId, IWallet wallet) {
        LOG.debug("setWallet: accountId={}, wallet={}", accountId, wallet);
        SessionHelper.getInstance().getTransactionData().setWallet(wallet);
    }

    public IWallet getWallet(long accountId) {
        IWallet wallet = SessionHelper.getInstance().getTransactionData().getWallet();
        if (wallet == null) {
            LOG.debug("getWallet: not found in transaction data, load from persister, accountId={}", accountId);
            wallet = persister.getWallet(accountId);
        }
        return wallet;
    }

    public void removeWallet(long accountId) {
        persister.removeWallet(accountId);
        SessionHelper.getInstance().getTransactionData().setWallet(null);
    }

    public void removeGameWallet(long accountId, int gameId) {
        persister.removeGameWallet(accountId, gameId);
    }

    @Override
    public IWallet getObject(String id) {
        return getWallet(Long.parseLong(id));
    }

    @Override
    public Map<String, IWallet> getAllObjects() {
        return Collections.emptyMap();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public String getAdditionalInfo() {
        return "";
    }

    @Override
    public String printDebug() {
        return "";
    }
}
