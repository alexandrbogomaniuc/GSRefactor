package com.dgphoenix.casino.common.cache.data.payment;

/**
 * User: plastical
 * Date: 04.03.2010
 */
public abstract class AbstractWallet {
    protected long accountId;

    protected AbstractWallet() {
    }

    public AbstractWallet(long accountId) {
        this.accountId = accountId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @Override
    public String toString() {
        return "accountId=" + accountId;
    }
}
