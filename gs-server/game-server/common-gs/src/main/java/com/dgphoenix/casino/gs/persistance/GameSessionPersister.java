package com.dgphoenix.casino.gs.persistance;

import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItemType;
import com.dgphoenix.casino.gs.managers.dblink.DBLinkCache;
import com.dgphoenix.casino.gs.managers.game.session.GameSessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 4/3/12
 */
public class GameSessionPersister {
    private static final Logger LOG = LogManager.getLogger(GameSessionPersister.class);
    private static GameSessionPersister instance = new GameSessionPersister();

    private GameSessionPersister() {
    }

    public static GameSessionPersister getInstance() {
        return instance;
    }

    public GameSession getGameSession(long gameSessionId) throws CommonException {
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        if (transactionData == null) {
            throw new CommonException("Transaction not started");
        }
        if (transactionData.getGameSession() != null && transactionData.getGameSession().getId() != gameSessionId) {
            LOG.warn("getGameSession: Wrong game session is found, transactionData=" + transactionData +
                    ", required gameSessionId=" + gameSessionId);
            throw new CommonException("wrong game session is found");
        }

        return transactionData.getGameSession();
    }

    public boolean isExist(long gameSessionId) {
        try {
            getGameSession(gameSessionId);
        } catch (CommonException e) {
            return false;
        }
        return true;
    }

    public void save(GameSession session) throws CommonException {
        SessionHelper.getInstance().getTransactionData().setGameSession(session);
    }

    public void transferGameSession(GameSession session, long bankId) throws CommonException {
        if (session != null) {
            final BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (bankInfo.isPersistGameSessions() && session.isRealMoney()) {
                SessionHelper.getInstance().getTransactionData().add(StoredItemType.GAME_SESSION, session, null);
            }
            GameSessionManager.getInstance().remove(session);
            DBLinkCache.getInstance().remove(session.getId());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeGameSession " + session);
        }
    }
}
