package com.dgphoenix.casino.common;

import com.dgphoenix.casino.common.cache.IAccountManager;
import com.dgphoenix.casino.common.lock.ILockManager;
import com.abs.casino.common.transactiondata.ITransactionDataCreator;
import com.abs.casino.common.transactiondata.ITransactionDataPersister;


/** Creates DomainSession objects with default dependencies */
public class DomainSessionFactory {

    /**  Default dependencies */
    private final ILockManager lockManager;
    private final ITransactionDataPersister persister;
    private final ITransactionDataCreator defaultTransactionDataCreator;
    private final IAccountManager accountManager;

    public DomainSessionFactory(ILockManager lockManager, ITransactionDataPersister persister,
                                ITransactionDataCreator defaultTransactionDataCreator, IAccountManager accountManager) {
        this.lockManager = lockManager;
        this.persister = persister;
        this.defaultTransactionDataCreator = defaultTransactionDataCreator;
        this.accountManager = accountManager;
    }

    public DomainSession createDomainSession() {
        return new DomainSession(lockManager, persister, defaultTransactionDataCreator, accountManager);
    }
}
