package com.dgphoenix.casino.common;

import com.dgphoenix.casino.common.cache.IAccountManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.DBException;
import com.abs.casino.common.exception.MismatchSessionException;
import com.dgphoenix.casino.common.lock.ILockManager;
import com.dgphoenix.casino.common.lock.LockingInfo;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.transactiondata.ITransactionDataCreator;
import com.dgphoenix.casino.common.transactiondata.ITransactionDataPersister;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

public class DomainSession {
    protected static final Logger LOG = LogManager.getLogger(DomainSession.class);

    private static final AtomicLong activeTransactionsCount = new AtomicLong(0);
    private ITransactionData transactionData;
    private LockingInfo lockInfo;
    private String sessionId = null;
    private volatile boolean inProgress = false;
    private final ILockManager lockManager;
    private final ITransactionDataPersister persister;
    private final ITransactionDataCreator defaultTransactionDataCreator;
    private final IAccountManager accountManager;

    static {
        StatisticsManager.getInstance().registerStatisticsGetter("DomainSession activeTransactionsCount",
                () -> String.valueOf(activeTransactionsCount.get()));
    }

    /**
     * @deprecated since 'persister' is no more a static field
     * Exists for backward compatibility with 'common-wallet' module.
     * @return ITransactionDataPersister for current thread session
     */
    @Deprecated
    public static ITransactionDataPersister getPersister() {
        return SessionHelper.getInstance().getDomainSession().getSessionPersister();
    }

    public DomainSession(ILockManager lockManager, ITransactionDataPersister persister,
                         ITransactionDataCreator transactionDataCreator,
                         IAccountManager accountManager) {
        this.lockManager = lockManager;
        this.persister = persister;
        this.defaultTransactionDataCreator = transactionDataCreator;
        this.accountManager = accountManager;
    }

    public ITransactionDataPersister getSessionPersister() {
        return persister;
    }

    public ITransactionData getTransactionData() {
        return transactionData;
    }

    public LockingInfo getLockInfo() {
        return lockInfo;
    }

    public static long getActiveTransactionsCount() {
        return activeTransactionsCount.get();
    }

    public void lock(long accountId) throws CommonException {
        Pair<Integer, String> pair = accountManager.getBankIdExternalIdByAccountId(accountId);
        lock(pair.getKey(), pair.getValue());
    }

    public void lock(String sessionId) throws CommonException {
        String lockId = StringIdGenerator.extractUserHash(sessionId);
        this.lockInfo = lockManager.lock(lockId);
        this.sessionId = sessionId;
    }

    public void lockByAccountHash(String lockId) throws CommonException {
        this.lockInfo = lockManager.lock(lockId);
    }

    public void lock(int bankId, String externalUserId) throws CommonException {
        String lockId = StringIdGenerator.getAccountHash(bankId, externalUserId);
        this.lockInfo = lockManager.lock(lockId);
    }

    public void lock(long accountId, long timeout) throws CommonException {
        Pair<Integer, String> pair = accountManager.getBankIdExternalIdByAccountId(accountId);
        lock(pair.getKey(), pair.getValue(), timeout);
    }

    public void lock(String sessionId, long timeout) throws CommonException {
        String lockId = StringIdGenerator.extractUserHash(sessionId);
        this.lockInfo = lockManager.lock(lockId, timeout);
        this.sessionId = sessionId;
    }

    public void lock(int bankId, String externalUserId, long timeout) throws CommonException {
        String lockId = StringIdGenerator.getAccountHash(bankId, externalUserId);
        this.lockInfo = lockManager.lock(lockId, timeout);
    }

    public void tryLock(long accountId) throws CommonException {
        Pair<Integer, String> pair = accountManager.getBankIdExternalIdByAccountId(accountId);
        tryLock(pair.getKey(), pair.getValue());
    }

    public void tryLock(String sessionId) throws CommonException {
        String lockId = StringIdGenerator.extractUserHash(sessionId);
        this.lockInfo = lockManager.tryLock(lockId);
        this.sessionId = sessionId;
    }

    public void tryLockByAccountHash(String lockId) throws CommonException {
        this.lockInfo = lockManager.tryLock(lockId);
    }

    public void tryLock(int bankId, String externalUserId) throws CommonException {
        String lockId = StringIdGenerator.getAccountHash(bankId, externalUserId);
        this.lockInfo = lockManager.tryLock(lockId);
    }

    public boolean isLockOwner() {
        return lockInfo != null && lockManager.isLockOwner(lockInfo.getLockId());
    }

    public boolean isSessionOpen() {
        return getTransactionData() != null;
    }

    public void openSession() throws MismatchSessionException {
        openSession(null);
    }

    public void openSession(ITransactionDataCreator creator) throws MismatchSessionException {
        assert lockInfo != null : "LockInfo is null";
        activeTransactionsCount.incrementAndGet();
        ITransactionData data = getTransactionData();
        if (data != null) {
            LOG.debug("Cannot open session, previous transaction data found={}", data);
            throw new IllegalStateException("Cannot open session, previous transaction data found");
        }
        data = persister.get(lockInfo);
        if (data == null) {
            data = (creator == null ? defaultTransactionDataCreator : creator).create(lockInfo.getLockId(),
                    persister.getGameServerId());
        }
        this.transactionData = data;
        inProgress = true;
        LOG.trace("session open[lockId={}]: {}", lockInfo.getLockId(), data);
        validateBySessionId(data);
    }

    protected void validateBySessionId(ITransactionData data) throws MismatchSessionException {
        if (sessionId != null && data != null &&
                data.getPlayerSession() != null &&
                !data.getPlayerSession().getSessionId().equals(sessionId)) {
            throw new MismatchSessionException("Mismatch sessionId. (received:" + sessionId +
                    "; expected:" + data.getPlayerSession().getSessionId() + ")");
        }
    }

    public void commitTransaction() throws DBException {
        final ITransactionData data = getTransactionData();
        if (data == null) {
            throw new DBException("Transaction data is null");
        }
        if (data.isNeedRemove()) {
            LOG.trace("commitTransaction(clear transactionData)");
            if (!StringUtils.isTrimmedEmpty(data.getLockId())) {
                persister.delete(data);
            }
        } else {
            persister.persist(data);
        }
    }

    public void markTransactionCompleted() {
        inProgress = false;
    }

    public boolean isTransactionStarted() {
        return getTransactionData() != null;
    }

    public void clear() {
        if (transactionData != null) {
            activeTransactionsCount.decrementAndGet();
        }
        transactionData = null;
        inProgress = false;
    }

    public void clearWithUnlock() {
        LockingInfo lock = this.lockInfo;
        boolean needInvalidate = this.inProgress;
        clear();
        this.lockInfo = null;
        this.sessionId = null;
        if (lock != null) {
            if (needInvalidate) {
                persister.invalidate(lock.getServerLockInfo());
            }
            lockManager.unlock(lock);
        }
    }

    public void persistWallet() {
        persister.persistWallet(transactionData);
    }

    public void persistAccount() {
        persister.persistAccount(transactionData);
    }

    public void persistFrbNotification() {
        persister.persistFrbNotification(transactionData);
    }

    public void persistFrbWin() {
        persister.persistFrbWin(transactionData);
    }

    public void persistPaymentTransaction(boolean saveAccount) {
        persister.persistPaymentTransaction(transactionData, saveAccount);
    }

    public void persistPlayerBet() {
        persister.persistPlayerBet(transactionData);
    }

    public void persistBonus() {
        persister.persistBonus(transactionData);
    }
}
