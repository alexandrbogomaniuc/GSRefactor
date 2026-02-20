package com.dgphoenix.casino.common;

import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.DBException;
import com.dgphoenix.casino.common.exception.MismatchSessionException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.transactiondata.ITransactionDataCreator;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonGameWallet;

import java.util.Map;

/**
 * Created by ANGeL
 * Date: Sep 16, 2008
 * Time: 5:07:10 PM
 */
public class SessionHelper {
    public static final String TOKEN = "token";
    private static final SessionHelper instance = new SessionHelper();
    private static final ThreadLocal<DomainSession> session = new ThreadLocal<>();

    private DomainSessionFactory domainSessionFactory;

    public static SessionHelper getInstance() {
        return instance;
    }

    private SessionHelper() {
    }

    public DomainSession getDomainSession() {
        DomainSession ds = session.get();
        if (ds == null) {
            ds = createDomainSession();
            session.set(ds);
        }
        return ds;
    }

    private DomainSession createDomainSession() {
        if (domainSessionFactory == null) {
            domainSessionFactory = ApplicationContextHelper.getBean(DomainSessionFactory.class);
        }
        return domainSessionFactory.createDomainSession();
    }

    public void clear() {
        getDomainSession().clear();
    }

    public void clearWithUnlock() {
        getDomainSession().clearWithUnlock();
    }

    public boolean isSessionOpen() {
        return getDomainSession().isSessionOpen();
    }

    public void openSession() throws MismatchSessionException {
        getDomainSession().openSession();
    }

    public void openSession(ITransactionDataCreator creator) throws MismatchSessionException {
        getDomainSession().openSession(creator);
    }

    public ITransactionData getTransactionData() {
        return getDomainSession().getTransactionData();
    }

    public void lock(long accountId) throws CommonException {
        if (accountId <= 0) {
            throw new CommonException("accountId is wrong: " + accountId);
        }
        getDomainSession().lock(accountId);
    }

    public void lock(String sessionId) throws CommonException {
        if (StringUtils.isTrimmedEmpty(sessionId)) {
            throw new CommonException("sessionId is empty");
        }
        getDomainSession().lock(sessionId);
    }

    //this is rarely used method for special cases
    public void lockByAccountHash(String lockId) throws CommonException {
        if (StringUtils.isTrimmedEmpty(lockId)) {
            throw new CommonException("lockId is empty");
        }
        getDomainSession().lockByAccountHash(lockId);
    }

    public void lock(int bankId, String externalUserId) throws CommonException {
        if (StringUtils.isTrimmedEmpty(externalUserId)) {
            throw new CommonException("externalUserId is empty");
        }
        getDomainSession().lock(bankId, externalUserId);
    }

    public void lock(long accountId, long timeout) throws CommonException {
        if (accountId <= 0) {
            throw new CommonException("accountId is wrong: " + accountId);
        }
        getDomainSession().lock(accountId, timeout);
    }

    public void lock(String sessionId, long timeout) throws CommonException {
        if (StringUtils.isTrimmedEmpty(sessionId)) {
            throw new CommonException("sessionId is empty");
        }
        getDomainSession().lock(sessionId, timeout);
    }

    public void lock(int bankId, String externalUserId, long timeout) throws CommonException {
        if (StringUtils.isTrimmedEmpty(externalUserId)) {
            throw new CommonException("externalUserId is empty");
        }
        getDomainSession().lock(bankId, externalUserId, timeout);
    }

    public void tryLock(long accountId) throws CommonException {
        if (accountId <= 0) {
            throw new CommonException("accountId is wrong: " + accountId);
        }
        getDomainSession().tryLock(accountId);
    }

    public void tryLock(String sessionId) throws CommonException {
        if (StringUtils.isTrimmedEmpty(sessionId)) {
            throw new CommonException("sessionId is empty");
        }
        getDomainSession().tryLock(sessionId);
    }

    //this is rarely used method for special cases
    public void tryLockByAccountHash(String lockId) throws CommonException {
        if (StringUtils.isTrimmedEmpty(lockId)) {
            throw new CommonException("lockId is empty");
        }
        getDomainSession().tryLockByAccountHash(lockId);
    }

    public void tryLock(int bankId, String externalUserId) throws CommonException {
        if (StringUtils.isTrimmedEmpty(externalUserId)) {
            throw new CommonException("externalUserId is empty");
        }
        getDomainSession().tryLock(bankId, externalUserId);
    }

    public boolean isLockOwner() {
        return getDomainSession().isLockOwner();
    }

    public void commitTransaction() throws DBException {
        getDomainSession().commitTransaction();
    }

    public void markTransactionCompleted() {
        getDomainSession().markTransactionCompleted();
    }

    public boolean isTransactionStarted() {
        return getDomainSession().isTransactionStarted();
    }

    public String getToken(long gameId) throws CommonException {
        Map<String, String> additionalParameters = getAdditionalParameters((int) gameId);
        return additionalParameters.get(TOKEN);
    }

    public void setToken(long gameId, String token) throws CommonException {
        setAdditionalParameters((int) gameId, TOKEN, token);
    }

    public Map<String, String> getAdditionalParameters(int gameId) throws CommonException {
        ITransactionData transactionData = getTransactionData();
        SessionInfo sessionInfo = transactionData.getPlayerSession();
        String additionalParameters;
        if (sessionInfo != null) {
            additionalParameters = sessionInfo.getSecretKey();
        } else {
            IWallet wallet = transactionData.getWallet();
            CommonGameWallet gameWallet = wallet == null ? null : wallet.getGameWallet(gameId);
            if (gameWallet != null) {
                additionalParameters = gameWallet.getAdditionalRoundInfo();
            } else {
                throw new CommonException("Unable to get additional params");
            }
        }
        return CollectionUtils.stringToMap(additionalParameters);
    }

    public void setAdditionalParameters(int gameId, String key, String value) throws CommonException {
        Map<String, String> params = getAdditionalParameters(gameId);
        params.put(key, value);
        setAdditionalParameters(gameId, params);
    }

    public void setAdditionalParameters(int gameId, Map<String, String> additionalParameters) throws CommonException {
        ITransactionData transactionData = getTransactionData();
        SessionInfo sessionInfo = transactionData.getPlayerSession();
        if (sessionInfo != null) {
            sessionInfo.setSecretKey(CollectionUtils.mapToString(additionalParameters));
        } else {
            IWallet wallet = transactionData.getWallet();
            CommonGameWallet gameWallet = wallet == null ? null : wallet.getGameWallet(gameId);
            if (gameWallet != null) {
                gameWallet.setAdditionalRoundInfo(CollectionUtils.mapToString(additionalParameters));
                return;
            }
            throw new CommonException("Unable to store additional params");
        }
    }
}
