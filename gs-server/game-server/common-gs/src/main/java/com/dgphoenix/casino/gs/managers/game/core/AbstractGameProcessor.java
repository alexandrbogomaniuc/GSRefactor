package com.dgphoenix.casino.gs.managers.game.core;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.logkit.GameLog;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.managers.dblink.DBLinkCache;
import com.dgphoenix.casino.gs.managers.dblink.IDBLink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created Date: 01.12.2008 Time: 17:37:04
 */
public abstract class AbstractGameProcessor implements IGameProcessor {
    private static final Logger LOG = LogManager.getLogger(AbstractGameProcessor.class);
    private final long gameId;

    protected AbstractGameProcessor(long gameId) {
        this.gameId = gameId;
    }

    @Override
    public long getGameId() {
        return gameId;
    }

    @Override
    public IDBLink registerAccount(AccountInfo accountInfo, GameMode mode, String lasthand, Long gameSessionId,
                                   Long bonusId, SessionInfo sessionInfo, IBaseGameInfo gameInfo, String lang,
                                   boolean restart)
            throws CommonException {
        long now = System.currentTimeMillis();
        if (gameSessionId == null) {
            gameSessionId = GameServer.getInstance().getIdGenerator().getNext(GameSession.class);
            if (LOG.isDebugEnabled()) {
                LOG.debug("registerAccount accountId:" + accountInfo.getId()
                        + " mode:" + mode + " game session id created, gameSessionId:" + gameSessionId);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("registerAccount accountId:" + accountInfo.getId() + " gameId:" + getGameId() +
                    " gameSessionId:" + gameSessionId + " mode:" + mode + " lastHand:" + lasthand);
        }
        StatisticsManager.getInstance().updateRequestStatistics("AbstractGameProcessor:registerAccount 01",
                System.currentTimeMillis() - now, accountInfo.getId());
        now = System.currentTimeMillis();

        IDBLink dblink;
        if (DBLinkCache.getInstance().isExist(gameSessionId)) {
            StatisticsManager.getInstance().updateRequestStatistics("AbstractGameProcessor:registerAccount 02",
                    System.currentTimeMillis() - now);
            now = System.currentTimeMillis();
            DBLinkCache.getInstance().remove(gameSessionId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("registerAccount old dbLink removed, gameSessionId=" + gameSessionId);
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics("AbstractGameProcessor:registerAccount 1",
                System.currentTimeMillis() - now, accountInfo.getId());
        now = System.currentTimeMillis();

        if (restart && mode == GameMode.FREE) {
            dblink = recreateDBLink(SessionHelper.getInstance().getTransactionData().getGameSession(), accountInfo,
                    false);
        } else {
            dblink = createDBLink(accountInfo, mode, gameSessionId, bonusId, sessionInfo, gameInfo, lang);
        }
        StatisticsManager.getInstance().updateRequestStatistics("AbstractGameProcessor:registerAccount 2",
                System.currentTimeMillis() - now, accountInfo.getId());
        now = System.currentTimeMillis();

        StatisticsManager.getInstance().updateRequestStatistics("AbstractGameProcessor:registerAccount 3",
                System.currentTimeMillis() - now, accountInfo.getId());
        now = System.currentTimeMillis();

        DBLinkCache.getInstance().put(dblink);
        StatisticsManager.getInstance().updateRequestStatistics("AbstractGameProcessor:registerAccount 4",
                System.currentTimeMillis() - now, accountInfo.getId());
        return dblink;
    }

    @Override
    public IDBLink getDBLink(long accountId) throws CommonException {
        throw new CommonException("load DBLink by accountId not supported");
        //return DBLinkCache.getInstance().getByAccountId(accountId);
    }

    @Override
    public void closeGameSession(GameSession gameSession, boolean limitsChanged, SessionInfo sessionInfo)
            throws CommonException {
        long now = System.currentTimeMillis();
        long accountId = gameSession.getAccountId();
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("closeGameSession gameId:" + getGameId() +
                        " for accountId:" + accountId + " limitsChanged:" + limitsChanged + " started");
            }

            long now1 = System.currentTimeMillis();

            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(accountId);
            if (accountInfo == null) {
                throw new CommonException("account doesn't exist, accountId:" + accountId);
            }

            IDBLink dbLink = DBLinkCache.getInstance().get(gameSession.getId());
            boolean needRecreateOnAutofinish = false;
            if (dbLink == null) {
                dbLink = recreateDBLink(gameSession, accountInfo, false);
                needRecreateOnAutofinish = true;
            }

            boolean needAutoFinish = sessionInfo != null && sessionInfo.getGameSessionId() != null &&
                    isBankSupported(accountInfo.getBankId()) && !isMultiplayerGame(gameSession.getGameId());
            if (needAutoFinish) {
                if (needRecreateOnAutofinish) {
                    DBLinkCache.getInstance().put(dbLink);
                }
                if (needRecreateOnAutofinish) {
                    DBLinkCache.getInstance().remove(dbLink.getGameSessionId());
                }
            }
            StatisticsManager.getInstance().updateRequestStatistics("AbstractGameProcessor: recreateDBLink",
                    System.currentTimeMillis() - now1, accountId);
            now1 = System.currentTimeMillis();

            if (dbLink.getGameSession() != null) {
                dbLink.finishGameSession(gameSession, sessionInfo);
                StatisticsManager.getInstance().updateRequestStatistics("AbstractGameProcessor: finishGameSession",
                        System.currentTimeMillis() - now1, accountId);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("closeGameSession gameId:" + getGameId() + " gameName:" + dbLink.getGameName() +
                            " for accountId:" + accountId + " OK");
                }
            }
        } finally {
            GameLog.getInstance().closeUserLevelLog();
        }
        StatisticsManager.getInstance().updateRequestStatistics("AbstractGameProcessor: closeGameSession",
                System.currentTimeMillis() - now, accountId);
    }

    private boolean isMultiplayerGame(long gameId) {
        return BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId).isMultiplayerGame();
    }

    private boolean isBankSupported(int bankId) {
        try {
            return BankInfoCache.getInstance().getBankInfo(bankId).isAutoFinishRequired();
        } catch (Exception e) {
            LOG.warn("Error isBankSupported bank {}", bankId);
            LOG.warn(e);
        }
        return false;
    }

    @Override
    public Map<String, String> processCommand(String cmd, Long accountId, HttpServletRequest request,
                                              HttpServletResponse response)
            throws IOException, ServletException, CommonException {

        IDBLink dbLink = getDBLink(accountId);
        if (dbLink == null) {
            throw new CommonException("DBLink not found");
        }

        return processCommand_internal(cmd, accountId, dbLink, request, response);
    }

    protected Map<String, String> processCommand_internal(String cmd, Long accountId, IDBLink dbLink,
                                                          HttpServletRequest request, HttpServletResponse response)
            throws CommonException {
        throw new CommonException("not implemented");
    }

    abstract protected IDBLink createDBLink(AccountInfo accountInfo, GameMode mode, Long gameSessionId,
                                            Long bonusId, SessionInfo sessionInfo, IBaseGameInfo gameInfo, String lang)
            throws CommonException;

    abstract public IDBLink recreateDBLink(GameSession gameSession, AccountInfo accountInfo, boolean putToCache) throws CommonException;
}
