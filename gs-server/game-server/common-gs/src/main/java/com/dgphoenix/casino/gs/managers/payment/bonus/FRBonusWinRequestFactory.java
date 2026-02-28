package com.dgphoenix.casino.gs.managers.payment.bonus;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraFrbWinOperationPersister;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBWinOperation;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.exception.DBException;
import com.abs.casino.common.exception.FRBException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.util.ReflectionUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.gs.managers.dblink.FRBonusDBLink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class FRBonusWinRequestFactory {

    private static final Logger LOG = LogManager.getLogger(FRBonusWinRequestFactory.class);
    private static final FRBonusWinRequestFactory instance = new FRBonusWinRequestFactory();
    private final Map<Long, IFRBonusWinManager> managers = new HashMap<Long, IFRBonusWinManager>();

    private final CassandraFrbWinOperationPersister frbWinOperationPersister;

    private FRBonusWinRequestFactory() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        frbWinOperationPersister = persistenceManager.getPersister(CassandraFrbWinOperationPersister.class);
    }

    public static FRBonusWinRequestFactory getInstance() {
        return instance;
    }

    public void interceptCreateFRBonusWin(AccountInfo accountInfo, long bankId, long gameSessionId, long gameId)
            throws FRBException {

        long now = System.currentTimeMillis();
        LOG.info("interceptCreateFRBonusWin, accountId=" + accountInfo.getId() + ", gameSessionId=" + gameSessionId +
                ", gameId=" + gameId);
        getFRBonusWinManager(bankId).handleCreateFRBonusWin(accountInfo, gameSessionId, gameId);
        StatisticsManager.getInstance().updateRequestStatistics("FRBonusWinRequestFactory::interceptCreateFRBonusWin",
                System.currentTimeMillis() - now);

    }

    public void interceptDestroyFRBonusWin(AccountInfo accountInfo, long bankId, long gameSessionId, long gameId,
                                           SessionInfo sessionInfo)
            throws FRBException, DBException {
        long now = System.currentTimeMillis();
        LOG.info("interceptDestroyFRBonusWin, accountId=" + accountInfo.getId() + ", gameSessionId=" + gameSessionId +
                ", gameId=" + gameId);
        if (sessionInfo == null) {
            LOG.info("interceptDestroyFRBonusWin: found frbWin for empty, accountId=" + accountInfo.getId() +
                    ", sessionInfo: " + sessionInfo + ", destroy frbWin");
            getFRBonusWinManager(bankId).handleDestroyFRBonusWin(accountInfo, gameId);
        } else {
            Long gameSessionId1 = sessionInfo.getGameSessionId();
            if (gameSessionId1 != null && gameSessionId1 == gameSessionId) {
                LOG.info("finishGameSession: found frbwin (for this),  accountId=" + accountInfo.getId() +
                        ", sessionInfo: " + sessionInfo + ", destroy FRBWin");
                getFRBonusWinManager(bankId).handleDestroyFRBonusWin(accountInfo, gameId);
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics("FRBonusWinRequestFactory::interceptDestroyFRBWin",
                System.currentTimeMillis() - now);

    }

    public void handleMPGameCredit(AccountInfo accountInfo, boolean isRoundFinished, long gameId,
                                   long gameSessionId, long bonusId, SessionInfo sessionInfo, long winAmount)
            throws FRBException {
        long now = System.currentTimeMillis();
        getFRBonusWinManager(accountInfo.getBankId()).handleMPGameCredit(accountInfo, isRoundFinished, gameId,
                gameSessionId, bonusId, sessionInfo, winAmount);
        StatisticsManager.getInstance().updateRequestStatistics("FRBonusWinRequestFactory::handleMPGameCredit",
                System.currentTimeMillis() - now);

    }

    public void interceptCredit(long accountId, FRBonusDBLink dbLink, boolean isRoundFinished) throws FRBException {
        long now = System.currentTimeMillis();
        getFRBonusWinManager(dbLink.getBankId()).handleCredit(accountId, isRoundFinished, dbLink);
        StatisticsManager.getInstance().updateRequestStatistics("FRBonusWinRequestFactory::interceptCredit",
                System.currentTimeMillis() - now);
    }

    public void interceptCreditCompleted(long accountId, FRBonusDBLink dbLink, boolean isRoundFinished) throws FRBException {
        long now = System.currentTimeMillis();
        getFRBonusWinManager(dbLink.getBankId()).handleCreditCompleted(accountId, isRoundFinished, dbLink);
        StatisticsManager.getInstance().updateRequestStatistics("FRBonusWinRequestFactory::interceptCreditCompleted",
                System.currentTimeMillis() - now);

    }

    public void interceptDebitCompleted(long accountId, FRBonusDBLink dbLink) throws FRBException {
        long now = System.currentTimeMillis();
        getFRBonusWinManager(dbLink.getBankId()).handleDebitCompleted(accountId, dbLink);
        StatisticsManager.getInstance().updateRequestStatistics("FRBonusWinRequestFactory::interceptDebitCompleted",
                System.currentTimeMillis() - now);

    }

    public IFRBonusWinManager getFRBonusWinManager(long bankId) throws FRBException {
        if (!isFRBWMExist(bankId)) {
            synchronized (this) {
                if (!isFRBWMExist(bankId)) {
                    instantiateFRBWM(bankId);
                }
            }
        }
        return getFRBWM(bankId);
    }

    public IFRBonusWinManager getFRBWM(long bankId) {
        return managers.get(bankId);
    }

    private synchronized IFRBonusWinManager instantiateFRBWM(long bankId) throws FRBException {
        LOG.info("FRBonusWinRequestFactory::instantiateFRBWM instantiating FRBWM for bankId:" + bankId);
        try {
            IFRBonusWinManager manager = null;
            String className = BankInfoCache.getInstance().getBankInfo(bankId).getFRBonusWinManager();
            if (!StringUtils.isTrimmedEmpty(className)) {
                Class<?> aClass = ReflectionUtils.forNameWithCompatibilityAliases(className);
                Constructor<?> wpmConstructor = aClass.getConstructor(long.class);
                manager = (IFRBonusWinManager) wpmConstructor.newInstance(bankId);
            }

            managers.put(bankId, manager);
            return manager;
        } catch (Exception e) {
            LOG.error("FRBonusWinRequestFactory::instantiateFRBWM error:", e);
            throw new FRBException(e);
        }
    }


    private boolean isFRBWMExist(long bankId) {
        return managers.containsKey(bankId);
    }

    protected void save(FRBWinOperation operation) throws FRBException {
        frbWinOperationPersister.persist(operation);
    }

    public void invalidateManager(long bankId) {
        managers.remove(bankId);
    }
}
